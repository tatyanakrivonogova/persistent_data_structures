package persistent.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent binary search tree with full deep copy on each modification.
 * Used for performance comparison against path-copying implementation.
 *
 * @param <T> the type of elements, must be Comparable
 */
@SuppressWarnings({"LineLength", "LongLine", "MaxLineLength"})
public final class PersistentBinaryTreeSlow<T extends Comparable<T>>
        implements PersistentStructure<T>, Collection<T> {

    /**
     * Mutable tree node for this implementation.
     *
     * @param <T> type of element in node, implements Comparable
     */
    private static final class Node<T extends Comparable<T>> {
        /** Stored value. */
        private T value;
        /** Left child. */
        private Node<T> left;
        /** Right child. */
        private Node<T> right;
        /** Node height. */
        private int height;
        /** Subtree size. */
        private int size;

        /**
         * Constructs a new node.
         *
         * @param nodeValue value to store
         * @param leftChild left child node
         * @param rightChild right child node
         */
        Node(final T nodeValue, final Node<T> leftChild, final Node<T> rightChild) {
            this.value = nodeValue;
            this.left = leftChild;
            this.right = rightChild;
            updateMetadata();
        }

        /**
         * Deep copy constructor.
         *
         * @param other the node to copy
         */
        Node(final Node<T> other) {
            this.value = other.value;
            this.left = other.left != null ? new Node<>(other.left) : null;
            this.right = other.right != null ? new Node<>(other.right) : null;
            updateMetadata();
        }

        /** Updates height and size. */
        void updateMetadata() {
            this.height = 1 + Math.max(height(left), height(right));
            this.size = 1 + size(left) + size(right);
        }

        /**
         * Returns height of node.
         *
         * @param node target node
         * @param <T> value type
         * @return height or 0 if null
         */
        static <T extends Comparable<T>> int height(final Node<T> node) {
            return node == null ? 0 : node.height;
        }

        /**
         * Returns subtree size.
         *
         * @param node target node
         * @param <T> value type
         * @return size or 0 if null
         */
        static <T extends Comparable<T>> int size(final Node<T> node) {
            return node == null ? 0 : node.size;
        }

        /**
         * Returns balance factor.
         *
         * @return height(left) - height(right)
         */
        int balanceFactor() {
            return height(left) - height(right);
        }

        /**
         * Creates deep copy of this node.
         *
         * @return deep copy
         */
        Node<T> deepCopy() {
            return new Node<>(this);
        }
    }

    /** Root node of the tree. */
    private final Node<T> root;

    /** Creates empty tree. */
    public PersistentBinaryTreeSlow() {
        this.root = null;
    }

    /**
     * Internal constructor.
     *
     * @param newRoot root node
     */
    private PersistentBinaryTreeSlow(final Node<T> newRoot) {
        this.root = newRoot;
    }

    @Override
    public PersistentStructure<T> createWithAdded(final T element) {
        if (element == null) {
            return this;
        }

        Node<T> newRoot = root != null ? root.deepCopy() : null;
        newRoot = insertMutable(newRoot, element);
        return new PersistentBinaryTreeSlow<>(newRoot);
    }

    @Override
    public PersistentStructure<T> createWithRemoved(final T element) {
        if (element == null || root == null) {
            return this;
        }
        Node<T> newRoot = root.deepCopy();
        newRoot = deleteMutable(newRoot, element);
        return new PersistentBinaryTreeSlow<>(newRoot);
    }

    @Override
    public PersistentStructure<T> createEmpty() {
        return new PersistentBinaryTreeSlow<>(null);
    }

    @Override
    public boolean containsElement(final T element) {
        if (element == null) {
            return false;
        }
        return contains(root, element);
    }

    @Override
    public boolean add(final T e) {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public boolean contains(final Object o) {
        try {
            @SuppressWarnings("unchecked")
            final T element = (T) o;
            return containsElement(element);
        } catch (final ClassCastException e) {
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
        final Object[] arr = new Object[size()];
        int i = 0;
        for (final T e : this) {
            arr[i++] = e;
        }
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(final E[] a) {
        final int s = size();
        E[] result = a;
        if (a.length < s) {
            result = (E[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), s);
        }

        int i = 0;
        for (final T e : this) {
            result[i++] = (E) e;
        }

        if (result.length > s) {
            result[s] = null;
        }

        return result;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        for (final Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(
                "PersistentBinaryTreeSlow is immutable.");
    }

    @Override
    public Version getVersion() {
        return new SimpleVersion();
    }

    @Override
    public PersistentStructure<T> snapshot() {
        if (root == null) {
            return new PersistentBinaryTreeSlow<>(null);
        }
        return new PersistentBinaryTreeSlow<>(root.deepCopy());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Collection<?>)) {
            return false;
        }

        final Collection<?> that = (Collection<?>) o;
        if (size() != that.size()) {
            return false;
        }

        final Iterator<T> it1 = iterator();
        final Iterator<?> it2 = that.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (final T e : this) {
            result = prime * result + (e == null ? 0 : e.hashCode());
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        final Iterator<T> it = iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Returns whether tree contains a value.
     *
     * @param node starting node
     * @param value target value
     * @return true if found
     */
    private boolean contains(final Node<T> node, final T value) {
        Node<T> cur = node;
        while (cur != null) {
            final int cmp = value.compareTo(cur.value);
            if (cmp < 0) {
                cur = cur.left;
            } else if (cmp > 0) {
                cur = cur.right;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursive insert.
     *
     * @param node starting node
     * @param value target value
     * @return node
     */
    private Node<T> insertMutable(final Node<T> node, final T value) {
        if (node == null) {
            return new Node<>(value, null, null);
        }
        final int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            node.left = insertMutable(node.left, value);
        } else if (cmp > 0) {
            node.right = insertMutable(node.right, value);
        } else {
            return node;
        }
        node.updateMetadata();
        return balanceMutable(node);
    }

    /**
     * Mutable delete.
     *
     * @param node starting node
     * @param value target value
     * @return node
     */
    private Node<T> deleteMutable(final Node<T> node, final T value) {
        if (node == null) {
            return null;
        }
        final int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            node.left = deleteMutable(node.left, value);
        } else if (cmp > 0) {
            node.right = deleteMutable(node.right, value);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            final Node<T> min = findMin(node.right);
            node.value = min.value;
            node.right = deleteMinMutable(node.right);
        }
        node.updateMetadata();
        return balanceMutable(node);
    }

    /**
     * Balances AVL-node.
     *
     * @param node starting node
     * @return node
     */
    private Node<T> balanceMutable(final Node<T> node) {
        if (node == null) {
            return null;
        }
        final int bf = node.balanceFactor();
        if (bf > 1) {
            if (node.left.balanceFactor() < 0) {
                node.left = rotateLeftMutable(node.left);
            }
            return rotateRightMutable(node);
        }
        if (bf < -1) {
            if (node.right.balanceFactor() > 0) {
                node.right = rotateRightMutable(node.right);
            }
            return rotateLeftMutable(node);
        }
        return node;
    }

    /**
     * Right rotation.
     *
     * @param y starting node
     * @return node
     */
    private Node<T> rotateRightMutable(final Node<T> y) {
        final Node<T> x = y.left;
        final Node<T> t2 = x.right;
        x.right = y;
        y.left = t2;
        y.updateMetadata();
        x.updateMetadata();
        return x;
    }

    /**
     * Left rotation.
     *
     * @param x starting node
     * @return node
     */
    private Node<T> rotateLeftMutable(final Node<T> x) {
        final Node<T> y = x.right;
        final Node<T> t2 = y.left;
        y.left = x;
        x.right = t2;
        x.updateMetadata();
        y.updateMetadata();
        return y;
    }

    /**
     * Finds minimum node.
     *
     * @param node starting node
     * @return min node
     */
    private Node<T> findMin(final Node<T> node) {
        Node<T> cur = node;
        while (cur.left != null) {
            cur = cur.left;
        }
        return cur;
    }

    /**
     * Finds maximum node.
     *
     * @param node starting node
     * @return max node
     */
    private Node<T> findMax(final Node<T> node) {
        Node<T> cur = node;
        while (cur.right != null) {
            cur = cur.right;
        }
        return cur;
    }

    /**
     * Finds min node.
     *
     * @param node starting node
     * @return min node
     */
    private Node<T> deleteMinMutable(final Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMinMutable(node.left);
        node.updateMetadata();
        return balanceMutable(node);
    }

    /** Iterator implementation. */
    private final class TreeIterator implements Iterator<T> {
        /** List of elements. */
        private final List<T> elements;
        /** Current index. */
        private int index;

        TreeIterator() {
            this.elements = new ArrayList<>();
            inOrderTraversal(root, elements::add);
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < elements.size();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return elements.get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "Persistent structures are immutable");
        }
    }

    /**
     * In-order traversal helper.
     *
     * @param  node node to work with
     * @param action action to use
     */
    private void inOrderTraversal(final Node<T> node,
                                  final java.util.function.Consumer<T> action) {
        if (node != null) {
            inOrderTraversal(node.left, action);
            action.accept(node.value);
            inOrderTraversal(node.right, action);
        }
    }

    /**
     * Returns minimum element.
     *
     * @return minimal element
     */
    public T min() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMin(root).value;
    }

    /**
     * Returns maximum element.
     *
     * @return maximum element
     */
    public T max() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMax(root).value;
    }

    /**
     * Returns tree height.
     *
     * @return height
     */
    public int height() {
        return Node.height(root);
    }

    /**
     * Checks if tree balanced.
     *
     * @return is balanced
     */
    public boolean isBalanced() {
        return isBalanced(root);
    }

    /**
     * Recursively checks balance.
     *
     * @param node node to check
     * @return maximum element
     */
    private boolean isBalanced(final Node<T> node) {
        if (node == null) {
            return true;
        }
        final int bf = node.balanceFactor();
        if (Math.abs(bf) > 1) {
            return false;
        }
        return isBalanced(node.left) && isBalanced(node.right);
    }
}
