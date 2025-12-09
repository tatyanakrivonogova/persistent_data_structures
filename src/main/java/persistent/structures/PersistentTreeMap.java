package persistent.structures;

import persistent.core.PersistentStructure;
import persistent.core.Version;
import persistent.core.SimpleVersion;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Persistent tree map that fully implements Collection interface.
 * Uses wrapper pattern for transactional updates.
 *
 * @param <K> the type of keys, must be Comparable
 * @param <V> the type of values
 */
public class PersistentTreeMap<K extends Comparable<K>, V>
        implements PersistentStructure<Map.Entry<K, V>> {
    
    /**
     * Immutable tree node.
     */
    private static class TreeNode<K extends Comparable<K>, V> {
        final K key;
        final V value;
        final TreeNode<K, V> left;
        final TreeNode<K, V> right;
        final int height;
        final int size;
        
        TreeNode(K key, V value, TreeNode<K, V> left, TreeNode<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.height = 1 + Math.max(height(left), height(right));
            this.size = 1 + size(left) + size(right);
        }
        
        static <K extends Comparable<K>, V> int height(TreeNode<K, V> node) {
            return node == null ? 0 : node.height;
        }
        
        static <K extends Comparable<K>, V> int size(TreeNode<K, V> node) {
            return node == null ? 0 : node.size;
        }
        
        int balanceFactor() {
            return height(left) - height(right);
        }
    }
    
    private final TreeNode<K, V> root;
    
    // Public constructor for empty map
    public PersistentTreeMap() {
        this.root = null;
    }
    
    // Private constructor for internal use
    private PersistentTreeMap(TreeNode<K, V> root) {
        this.root = root;
    }
    
    // ========== PersistentStructure interface implementations ==========
    
    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithAdded(Map.Entry<K, V> entry) {
        return put(entry.getKey(), entry.getValue());
    }
    
    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithRemoved(Map.Entry<K, V> entry) {
        return remove(entry.getKey());
    }
    
    @Override
    public PersistentStructure<Map.Entry<K, V>> createEmpty() {
        return new PersistentTreeMap<>(null);
    }
    
    @Override
    public boolean containsElement(Map.Entry<K, V> entry) {
        V value = get(entry.getKey());
        return value != null && value.equals(entry.getValue());
    }
    
    // ========== Map-specific operations ==========
    
    /**
     * Associates the specified value with the specified key.
     */
    public PersistentTreeMap<K, V> put(K key, V value) {
        TreeNode<K, V> newRoot = put(root, key, value);
        if (newRoot == root && get(key) != null && Objects.equals(get(key), value)) {
            return this; // No change
        }
        return new PersistentTreeMap<>(newRoot);
    }
    
    /**
     * Removes the mapping for the specified key.
     */
    public PersistentTreeMap<K, V> remove(K key) {
        TreeNode<K, V> newRoot = delete(root, key);
        if (newRoot == root) {
            return this; // Key not found
        }
        return new PersistentTreeMap<>(newRoot == null ? null : newRoot);
    }
    
    /**
     * Returns the value to which the specified key is mapped.
     */
    public V get(K key) {
        if (key == null) return null;
        TreeNode<K, V> node = get(root, key);
        return node == null ? null : node.value;
    }
    
    /**
     * Returns true if this map contains the specified key.
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    /**
     * Returns true if this map contains the specified value.
     */
    public boolean containsValue(V value) {
        return containsValue(root, value);
    }
    
    /**
     * Returns the first (lowest) key.
     */
    public K firstKey() {
        if (root == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return findMin(root).key;
    }
    
    /**
     * Returns the last (highest) key.
     */
    public K lastKey() {
        if (root == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return findMax(root).key;
    }
    
    // ========== Collection interface methods ==========
    
    @Override
    public boolean add(Map.Entry<K, V> entry) {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public boolean contains(Object o) {
        try {
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            return containsElement(entry);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public int size() {
        return TreeNode.size(root);
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new MapIterator();
    }
    
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        int i = 0;
        for (Map.Entry<K, V> entry : this) {
            array[i++] = entry;
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
        for (Map.Entry<K, V> entry : this) {
            a[i++] = (E) entry;
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
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
            "PersistentTreeMap is immutable. Use TransactionalPersistentTreeMap wrapper for mutable operations.");
    }
    
    @Override
    public Version getVersion() {
        return new SimpleVersion();
    }
    
    @Override
    public PersistentStructure<Map.Entry<K, V>> snapshot() {
        return this; // Already immutable
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collection)) return false;
        
        Collection<?> that = (Collection<?>) o;
        if (size() != that.size()) return false;
        
        Iterator<Map.Entry<K, V>> it1 = iterator();
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
        for (Map.Entry<K, V> entry : this) {
            result = 31 * result + (entry == null ? 0 : entry.hashCode());
        }
        return result;
    }
    
    @Override
    public String toString() {
        Iterator<Map.Entry<K, V>> it = iterator();
        if (!it.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Map.Entry<K, V> e = it.next();
            sb.append(e.getKey()).append('=').append(e.getValue());
            if (!it.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
    
    // ========== Tree operations ==========
    
    private TreeNode<K, V> get(TreeNode<K, V> node, K key) {
        if (key == null || node == null) return null;
        
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node;
        }
    }
    
    private TreeNode<K, V> put(TreeNode<K, V> node, K key, V value) {
        if (key == null) {
            return node; // Don't insert null key
        }
        
        if (node == null) {
            return new TreeNode<>(key, value, null, null);
        }
        
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            TreeNode<K, V> newLeft = put(node.left, key, value);
            if (newLeft == node.left && get(key) != null && Objects.equals(get(key), value)) {
                return node; // No change
            }
            return balance(new TreeNode<>(node.key, node.value, newLeft, node.right));
        } else if (cmp > 0) {
            TreeNode<K, V> newRight = put(node.right, key, value);
            if (newRight == node.right && get(key) != null && Objects.equals(get(key), value)) {
                return node; // No change
            }
            return balance(new TreeNode<>(node.key, node.value, node.left, newRight));
        } else {
            // Update existing key
            return new TreeNode<>(key, value, node.left, node.right);
        }
    }
    
    private TreeNode<K, V> delete(TreeNode<K, V> node, K key) {
        if (key == null || node == null) {
            return node;
        }
        
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            TreeNode<K, V> newLeft = delete(node.left, key);
            if (newLeft == node.left) {
                return node;
            }
            return balance(new TreeNode<>(node.key, node.value, newLeft, node.right));
        } else if (cmp > 0) {
            TreeNode<K, V> newRight = delete(node.right, key);
            if (newRight == node.right) {
                return node;
            }
            return balance(new TreeNode<>(node.key, node.value, node.left, newRight));
        } else {
            // Node to delete found
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            
            // Node with two children
            TreeNode<K, V> minNode = findMin(node.right);
            TreeNode<K, V> newRight = deleteMin(node.right);
            return balance(new TreeNode<>(minNode.key, minNode.value, node.left, newRight));
        }
    }
    
    private boolean containsValue(TreeNode<K, V> node, V value) {
        if (node == null) return false;
        
        if (Objects.equals(value, node.value)) {
            return true;
        }
        
        return containsValue(node.left, value) || containsValue(node.right, value);
    }
    
    private TreeNode<K, V> balance(TreeNode<K, V> node) {
        if (node == null) return null;
        
        int balanceFactor = node.balanceFactor();
        
        if (balanceFactor > 1) {
            if (node.left.balanceFactor() < 0) {
                // Left-Right case
                return rotateRight(new TreeNode<>(
                    node.key, node.value,
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
                return rotateLeft(new TreeNode<>(
                    node.key, node.value,
                    node.left,
                    rotateRight(node.right)
                ));
            }
            // Right-Right case
            return rotateLeft(node);
        }
        
        return node;
    }
    
    private TreeNode<K, V> rotateRight(TreeNode<K, V> y) {
        TreeNode<K, V> x = y.left;
        TreeNode<K, V> t2 = x.right;
        
        return new TreeNode<>(
            x.key, x.value,
            x.left,
            new TreeNode<>(y.key, y.value, t2, y.right)
        );
    }
    
    private TreeNode<K, V> rotateLeft(TreeNode<K, V> x) {
        TreeNode<K, V> y = x.right;
        TreeNode<K, V> t2 = y.left;
        
        return new TreeNode<>(
            y.key, y.value,
            new TreeNode<>(x.key, x.value, x.left, t2),
            y.right
        );
    }
    
    private TreeNode<K, V> findMin(TreeNode<K, V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    
    private TreeNode<K, V> findMax(TreeNode<K, V> node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }
    
    private TreeNode<K, V> deleteMin(TreeNode<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        return balance(new TreeNode<>(node.key, node.value, deleteMin(node.left), node.right));
    }
    
    private boolean isBalanced(TreeNode<K, V> node) {
        if (node == null) return true;
        
        int balanceFactor = node.balanceFactor();
        if (Math.abs(balanceFactor) > 1) {
            return false;
        }
        
        return isBalanced(node.left) && isBalanced(node.right);
    }
    
    // ========== Iterator ==========
    
    private class MapIterator implements Iterator<Map.Entry<K, V>> {
        private final List<Map.Entry<K, V>> entries;
        private int currentIndex;
        
        MapIterator() {
            this.entries = new ArrayList<>();
            inOrderTraversal(root, entries);
            this.currentIndex = 0;
        }
        
        @Override
        public boolean hasNext() {
            return currentIndex < entries.size();
        }
        
        @Override
        public Map.Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return entries.get(currentIndex++);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                "Persistent structures are immutable");
        }
    }
    
    private void inOrderTraversal(TreeNode<K, V> node, List<Map.Entry<K, V>> result) {
        if (node != null) {
            inOrderTraversal(node.left, result);
            result.add(new AbstractMap.SimpleEntry<>(node.key, node.value));
            inOrderTraversal(node.right, result);
        }
    }
    
    // ========== Utility methods ==========
    
    public int height() {
        return TreeNode.height(root);
    }
    
    public boolean isBalanced() {
        return isBalanced(root);
    }
}