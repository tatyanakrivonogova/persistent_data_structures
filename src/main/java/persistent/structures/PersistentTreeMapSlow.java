package persistent.structures;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent tree map that fully implements Map interface.
 *
 * @param <K> key type, must be Comparable
 * @param <V> value type
 */
@SuppressWarnings({"HiddenField"})
public final class PersistentTreeMapSlow<K extends Comparable<K>, V>
        implements PersistentStructure<Map.Entry<K, V>>, Map<K, V> {

    /**
     * Immutable tree node.
     * @param <K> the type of keys, must implement Comparable<K>
     * @param <V> the type of mapped values
     */
    private static final class Node<K, V> {
        /** The key stored in this node. */
        private final K key;
        /** The value stored in this node. */
        private final V value;
        /** The left child node. */
        private final Node<K, V> left;
        /** The right child node. */
        private final Node<K, V> right;

        /**
         * Constructs a new tree node.
         *
         * @param key the key
         * @param value the value
         * @param left the left child
         * @param right the right child
         */
        Node(final K key,
             final V value,
             final Node<K, V> left,
             final Node<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    /** The root node of this tree map. */
    private final Node<K, V> root;

    /** Creates empty map. */
    public PersistentTreeMapSlow() {
        this.root = null;
    }

    /**
     * Private constructor for internal use.
     * @param root new root node
     */
    private PersistentTreeMapSlow(final Node<K, V> root) {
        this.root = root;
    }

    // ========== PersistentStructure interface implementations ==========

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithAdded(
            final Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return this;
        }
        return putInternal(entry.getKey(), entry.getValue());
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithRemoved(
            final Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return this;
        }
        return removeInternal(entry.getKey());
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> createEmpty() {
        return new PersistentTreeMapSlow<>();
    }

    @Override
    public boolean containsElement(final Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return false;
        }
        V value = getInternal(entry.getKey());
        return Objects.equals(value, entry.getValue());
    }

    @Override
    public Version getVersion() {
        return new SimpleVersion();
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> snapshot() {
        return this;
    }

    @Override
    public int size() {
        return size(root);
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    // ========== Map interface methods (immutable ones) ==========

    @Override
    public boolean containsKey(final Object key) {
        try {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return getInternal(k) != null;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        try {
            @SuppressWarnings("unchecked")
            V v = (V) value;
            return containsValue(root, v);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public V get(final Object key) {
        try {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return getInternal(k);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new Values();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException(
            "PersistentTreeMapSlow is immutable. Use "
                + "TransactionalPersistentTreeMapSlow for mutable operations.");
    }

    // ========== Map interface methods (throw UnsupportedOperationException) ==========

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException(
            "PersistentTreeMapSlow is immutable. Use "
                + "TransactionalPersistentTreeMapSlow for mutable operations.");
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException(
            "PersistentTreeMapSlow is immutable. Use "
                + "TransactionalPersistentTreeMapSlow for mutable operations.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(
            "PersistentTreeMapSlow is immutable. Use "
                + "TransactionalPersistentTreeMapSlow for mutable operations.");
    }

    // ========== Persistent map-specific methods ==========

    /**
     * Associates the specified value with the specified key (persistent version).
     *
     * @param key the key
     * @param value the value
     * @return new map version with the key-value pair added or updated
     */
    public PersistentTreeMapSlow<K, V> putInternal(final K key, final V value) {
        if (key == null) {
            return this;
        }
        V oldValue = getInternal(key);
        if (Objects.equals(oldValue, value)) {
            return this;
        }
        Node<K, V> newRoot = putCopy(root, key, value);
        return new PersistentTreeMapSlow<>(newRoot);
    }

    /**
     * Removes the mapping for the specified key (persistent version).
     *
     * @param key the key to remove
     * @return new map version with the key removed
     */
    public PersistentTreeMapSlow<K, V> removeInternal(final K key) {
        if (!containsKey(key)) {
            return this;
        }
        Node<K, V> newRoot = removeCopy(root, key);
        return new PersistentTreeMapSlow<>(newRoot);
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key
     * @return the value associated with the key, or null if not found
     */
    public V getInternal(final K key) {
        if (key == null) {
            return null;
        }
        Node<K, V> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) {
                cur = cur.left;
            } else if (cmp > 0) {
                cur = cur.right;
            } else {
                return cur.value;
            }
        }
        return null;
    }

    // ========== Tree operations ==========

    /**
     * Method for internal use.
     *
     * @param key the key
     * @param value the value
     * @param node node-destination
     * @return new map version with the key-value pair added or updated
     */
    private Node<K, V> putCopy(final Node<K, V> node,
                               final K key,
                               final V value) {
        if (node == null) {
            return new Node<>(key, value, null, null);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return new Node<>(node.key, node.value,
                    putCopy(node.left, key, value), node.right);
        }
        if (cmp > 0) {
            return new Node<>(node.key, node.value,
                    node.left, putCopy(node.right, key, value));
        }
        return new Node<>(key, value, node.left, node.right);
    }

    /**
     * Method for internal use.
     *
     * @param key the key to remove
     * @param node node-destination
     * @return new map version with the key removed
     */
    private Node<K, V> removeCopy(final Node<K, V> node,
                                  final K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return new Node<>(node.key, node.value,
                    removeCopy(node.left, key), node.right);
        }
        if (cmp > 0) {
            return new Node<>(node.key, node.value,
                    node.left, removeCopy(node.right, key));
        }
        if (node.left == null) {
            return node.right;
        }
        if (node.right == null) {
            return node.left;
        }
        Node<K, V> min = findMin(node.right);
        Node<K, V> newRight = deleteMinCopy(node.right);
        return new Node<>(min.key, min.value, node.left, newRight);
    }

    /**
     * Returns true if this node contains the specified value.
     *
     * @param value the value
     * @param node node to look in
     * @return true if the map contains the value
     */
    private boolean containsValue(final Node<K, V> node,
                                  final V value) {
        if (node == null) {
            return false;
        }
        if (Objects.equals(node.value, value)) {
            return true;
        }
        return containsValue(node.left, value)
                || containsValue(node.right, value);
    }

    private Node<K, V> findMin(final Node<K, V> node) {
        Node<K, V> currentNode = node;
        while (currentNode.left != null) {
            currentNode = currentNode.left;
        }
        return currentNode;
    }

    private Node<K, V> deleteMinCopy(final Node<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        return new Node<>(node.key, node.value,
                deleteMinCopy(node.left), node.right);
    }

    private int size(final Node<K, V> node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left) + size(node.right);
    }

    // ========== EntrySet, KeySet, and Values implementations ==========

    private final class EntrySet implements Set<Map.Entry<K, V>> {
        @Override
        public int size() {
            return PersistentTreeMapSlow.this.size();
        }

        @Override
        public boolean isEmpty() {
            return PersistentTreeMapSlow.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            V value = PersistentTreeMapSlow.this.get(entry.getKey());
            return Objects.equals(value, entry.getValue());
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public Object[] toArray() {
            List<Map.Entry<K, V>> list = new ArrayList<>();
            for (Map.Entry<K, V> entry : this) {
                list.add(entry);
            }
            return list.toArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            List<Map.Entry<K, V>> list = new ArrayList<>();
            for (Map.Entry<K, V> entry : this) {
                list.add(entry);
            }
            return list.toArray(a);
        }

        @Override
        public boolean add(final Map.Entry<K, V> e) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(
            final Collection<? extends Map.Entry<K, V>> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Set<?> that = (Set<?>) o;
            if (size() != that.size()) {
                return false;
            }
            return containsAll(that);
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (Map.Entry<K, V> entry : this) {
                h += entry.hashCode();
            }
            return h;
        }
    }

    private final class KeySet implements Set<K> {
        @Override
        public int size() {
            return PersistentTreeMapSlow.this.size();
        }

        @Override
        public boolean isEmpty() {
            return PersistentTreeMapSlow.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return PersistentTreeMapSlow.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public Object[] toArray() {
            List<K> list = new ArrayList<>();
            for (K key : this) {
                list.add(key);
            }
            return list.toArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            List<K> list = new ArrayList<>();
            for (K key : this) {
                list.add(key);
            }
            return list.toArray(a);
        }

        @Override
        public boolean add(final K e) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(final Collection<? extends K> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Set<?> that = (Set<?>) o;
            if (size() != that.size()) {
                return false;
            }
            return containsAll(that);
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (K key : this) {
                h += key.hashCode();
            }
            return h;
        }
    }

    private final class Values implements Collection<V> {
        @Override
        public int size() {
            return PersistentTreeMapSlow.this.size();
        }

        @Override
        public boolean isEmpty() {
            return PersistentTreeMapSlow.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return PersistentTreeMapSlow.this.containsValue(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public Object[] toArray() {
            List<V> list = new ArrayList<>();
            for (V value : this) {
                list.add(value);
            }
            return list.toArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            List<V> list = new ArrayList<>();
            for (V value : this) {
                list.add(value);
            }
            return list.toArray(a);
        }

        @Override
        public boolean add(final V e) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(final Collection<? extends V> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(
                "PersistentTreeMapSlow is immutable");
        }
    }

    // ========== Iterators ==========

    private abstract class TreeIterator<E> implements Iterator<E> {
        /** The entries in order. */
        protected final List<E> elements; // Изменено с private на protected
        /** The current index in the iteration. */
        private int currentIndex;

        /** Creates a new iterator. */
        TreeIterator() {
        this.elements = new ArrayList<>();
        fillElements();
        this.currentIndex = 0;
        }

        abstract void fillElements();

        @Override
        public boolean hasNext() {
        return currentIndex < elements.size();
        }

        @Override
        public E next() {
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

    private final class EntryIterator extends TreeIterator<Map.Entry<K, V>> {
        @Override
        void fillElements() {
        inorder(root, elements);
        }
    }

    private final class KeyIterator extends TreeIterator<K> {
        @Override
        void fillElements() {
        inorderKeys(root, elements);
        }
    }

    private final class ValueIterator extends TreeIterator<V> {
        @Override
        void fillElements() {
        inorderValues(root, elements);
        }
    }

    private void inorder(final Node<K, V> node,
                         final List<Map.Entry<K, V>> out) {
        if (node == null) {
            return;
        }
        inorder(node.left, out);
        out.add(new AbstractMap.SimpleEntry<>(node.key, node.value));
        inorder(node.right, out);
    }

    private void inorderKeys(final Node<K, V> node,
                             final List<K> out) {
        if (node == null) {
            return;
        }
        inorderKeys(node.left, out);
        out.add(node.key);
        inorderKeys(node.right, out);
    }

    private void inorderValues(final Node<K, V> node,
                               final List<V> out) {
        if (node == null) {
            return;
        }
        inorderValues(node.left, out);
        out.add(node.value);
        inorderValues(node.right, out);
    }

    // ========== Utility methods ==========

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }

        Map<?, ?> that = (Map<?, ?>) o;
        if (size() != that.size()) {
            return false;
        }

        for (Map.Entry<K, V> entry : entrySet()) {
            Object value = that.get(entry.getKey());
            if (!Objects.equals(entry.getValue(), value)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Map.Entry<K, V> entry : entrySet()) {
            h += entry.hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (!it.hasNext()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Map.Entry<K, V> e = it.next();
            sb.append(e.getKey()).append('=').append(e.getValue());
            if (!it.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }
}
