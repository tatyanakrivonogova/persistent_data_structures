package persistent.structures;

import persistent.core.PersistentStructure;
import persistent.core.Version;
import persistent.core.SimpleVersion;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Persistent binary search tree that fully implements Collection interface.
 * Uses wrapper pattern for transactional updates.
 *
 * @param <T> the type of elements, must be Comparable
 */
public class PersistentBinaryTree<T extends Comparable<T>>
        implements PersistentStructure<T> {
    
    /**
     * Immutable tree node.
     */
    private static class Node<T extends Comparable<T>> {
        final T value;
        final Node<T> left;
        final Node<T> right;
        final int height;
        final int size;
        
        Node(T value, Node<T> left, Node<T> right) {
            this.value = value;
            this.left = left;
            this.right = right;
            this.height = 1 + Math.max(height(left), height(right));
            this.size = 1 + size(left) + size(right);
        }
        
        static <T extends Comparable<T>> int height(Node<T> node) {
            return node == null ? 0 : node.height;
        }
        
        static <T extends Comparable<T>> int size(Node<T> node) {
            return node == null ? 0 : node.size;
        }
        
        int balanceFactor() {
            return height(left) - height(right);
        }
    }
    
    private final Node<T> root;
    
    // Public constructor for empty tree
    public PersistentBinaryTree() {
        this.root = null;
    }
    
    // Private constructor for internal use
    private PersistentBinaryTree(Node<T> root) {
        this.root = root;
    }
    
    // ========== PersistentStructure interface implementations ==========
    
    @Override
    public PersistentStructure<T> createWithAdded(T element) {
        Node<T> newRoot = insert(root, element);
        if (newRoot == root) {
            return this; // Element already exists
        }
        return new PersistentBinaryTree<>(newRoot);
    }
    
    @Override
    public PersistentStructure<T> createWithRemoved(T element) {
        Node<T> newRoot = delete(root, element);
        if (newRoot == root) {
            return this; // Element not found
        }
        return new PersistentBinaryTree<>(newRoot == null ? null : newRoot);
    }
    
    @Override
    public PersistentStructure<T> createEmpty() {
        return new PersistentBinaryTree<>(null);
    }
    
    @Override
    public boolean containsElement(T element) {
        if (element == null) return false;
        return contains(root, element);
    }
    
    // ========== Collection interface methods ==========
    
    @Override
    public boolean add(T e) {
        // For PersistentBinaryTree, add() should throw since we're immutable
        // Users should use transactional wrapper
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public boolean contains(Object o) {
        try {
            @SuppressWarnings("unchecked")
            T element = (T) o;
            return containsElement(element);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public int size() {
        return Node.size(root);
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new TreeIterator();
    }
    
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        int i = 0;
        for (T element : this) {
            array[i++] = element;
        }
        return array;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        int size = size();
        if (a.length < size) {
            a = (E[]) java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
        }
        
        int i = 0;
        for (T element : this) {
            a[i++] = (E) element;
        }
        
        if (a.length > size) {
            a[size] = null;
        }
        
        return a;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
            "PersistentBinaryTree is immutable. Use TransactionalPersistentTree wrapper for mutable operations.");
    }
    
    @Override
    public Version getVersion() {
        // Generate version based on content hash
        return new SimpleVersion();
    }
    
    @Override
    public PersistentStructure<T> snapshot() {
        return this; // Already immutable
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collection)) return false;
        
        Collection<?> that = (Collection<?>) o;
        if (size() != that.size()) return false;
        
        Iterator<T> it1 = iterator();
        Iterator<?> it2 = that.iterator();
        
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        
        return !it1.hasNext() && !it2.hasNext();
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (T element : this) {
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }
    
    @Override
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            T e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
    
    // ========== Tree operations ==========
    
    private boolean contains(Node<T> node, T value) {
        if (value == null) return false;
        
        while (node != null) {
            int cmp = value.compareTo(node.value);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return true;
            }
        }
        return false;
    }
    
    private Node<T> insert(Node<T> node, T value) {
        if (value == null) {
            return node; // Don't insert null
        }
        
        if (node == null) {
            return new Node<>(value, null, null);
        }
        
        int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            Node<T> newLeft = insert(node.left, value);
            if (newLeft == node.left) {
                return node; // No change
            }
            return balance(new Node<>(node.value, newLeft, node.right));
        } else if (cmp > 0) {
            Node<T> newRight = insert(node.right, value);
            if (newRight == node.right) {
                return node; // No change
            }
            return balance(new Node<>(node.value, node.left, newRight));
        } else {
            return node; // Value already exists
        }
    }
    
    private Node<T> delete(Node<T> node, T value) {
        if (value == null || node == null) {
            return node;
        }
        
        int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            Node<T> newLeft = delete(node.left, value);
            if (newLeft == node.left) {
                return node;
            }
            return balance(new Node<>(node.value, newLeft, node.right));
        } else if (cmp > 0) {
            Node<T> newRight = delete(node.right, value);
            if (newRight == node.right) {
                return node;
            }
            return balance(new Node<>(node.value, node.left, newRight));
        } else {
            // Node to delete found
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            
            // Node with two children
            Node<T> minNode = findMin(node.right);
            Node<T> newRight = deleteMin(node.right);
            return balance(new Node<>(minNode.value, node.left, newRight));
        }
    }
    
    private Node<T> balance(Node<T> node) {
        if (node == null) return null;
        
        int balanceFactor = node.balanceFactor();
        
        if (balanceFactor > 1) {
            if (node.left.balanceFactor() < 0) {
                // Left-Right case
                return rotateRight(new Node<>(
                    node.value,
                    rotateLeft(node.left),
                    node.right
                ));
            }
            // Left-Left case
            return rotateRight(node);
        }
        
        if (balanceFactor < -1) {
            if (node.right.balanceFactor() > 0) {
                // Right-Left case
                return rotateLeft(new Node<>(
                    node.value,
                    node.left,
                    rotateRight(node.right)
                ));
            }
            // Right-Right case
            return rotateLeft(node);
        }
        
        return node;
    }
    
    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.left;
        Node<T> t2 = x.right;
        
        return new Node<>(
            x.value,
            x.left,
            new Node<>(y.value, t2, y.right)
        );
    }
    
    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.right;
        Node<T> t2 = y.left;
        
        return new Node<>(
            y.value,
            new Node<>(x.value, x.left, t2),
            y.right
        );
    }
    
    private Node<T> findMin(Node<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    
    private Node<T> findMax(Node<T> node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }
    
    private Node<T> deleteMin(Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        return balance(new Node<>(node.value, deleteMin(node.left), node.right));
    }
    
    // ========== Iterator ==========
    
    private class TreeIterator implements Iterator<T> {
        private final List<T> elements;
        private int currentIndex;
        
        TreeIterator() {
            this.elements = new ArrayList<>();
            inOrderTraversal(root, elements::add);
            this.currentIndex = 0;
        }
        
        @Override
        public boolean hasNext() {
            return currentIndex < elements.size();
        }
        
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return elements.get(currentIndex++);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                "Persistent structures are immutable");
        }
    }
    
    private void inOrderTraversal(Node<T> node, java.util.function.Consumer<T> action) {
        if (node != null) {
            inOrderTraversal(node.left, action);
            action.accept(node.value);
            inOrderTraversal(node.right, action);
        }
    }
    
    // ========== Utility methods ==========
    
    public T min() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMin(root).value;
    }
    
    public T max() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMax(root).value;
    }
    
    public int height() {
        return Node.height(root);
    }
    
    public boolean isBalanced() {
        return isBalanced(root);
    }
    
    private boolean isBalanced(Node<T> node) {
        if (node == null) return true;
        
        int balanceFactor = node.balanceFactor();
        if (Math.abs(balanceFactor) > 1) {
            return false;
        }
        
        return isBalanced(node.left) && isBalanced(node.right);
    }
}