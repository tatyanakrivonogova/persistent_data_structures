package persistent.structures;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent tree map that fully implements Collection interface.
 * Uses wrapper pattern for transactional updates.
 *
 * @param <K> key type, must be Comparable
 * @param <V> value type
 */
public final class PersistentTreeMapSlow<K extends Comparable<K>, V>
        implements PersistentStructure<Map.Entry<K, V>> {

    /** Immutable tree node. */
    private static final class Node<K, V> {
        private final K key;
        private final V value;
        private final Node<K, V> left;
        private final Node<K, V> right;

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

    private final Node<K, V> root;

    /** Creates empty map. */
    public PersistentTreeMapSlow() {
        this.root = null;
    }

    private PersistentTreeMapSlow(final Node<K, V> root) {
        this.root = root;
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithAdded(
            final Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return this;
        }
        return put(entry.getKey(), entry.getValue());
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithRemoved(
            final Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return this;
        }
        return remove(entry.getKey());
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
        V value = get(entry.getKey());
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

    /** Returns value by key. */
    public V get(final K key) {
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

    /** Checks key existence. */
    public boolean containsKey(final K key) {
        return get(key) != null;
    }

    /** Checks value existence. */
    public boolean containsValue(final V value) {
        return containsValue(root, value);
    }

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

    /** Adds or replaces value. */
    public PersistentTreeMapSlow<K, V> put(final K key, final V value) {
        if (key == null) {
            return this;
        }
        V oldValue = get(key);
        if (Objects.equals(oldValue, value)) {
            return this;
        }
        Node<K, V> newRoot = putCopy(root, key, value);
        return new PersistentTreeMapSlow<>(newRoot);
    }

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

    /** Removes key. */
    public PersistentTreeMapSlow<K, V> remove(final K key) {
        if (!containsKey(key)) {
            return this;
        }
        Node<K, V> newRoot = removeCopy(root, key);
        return new PersistentTreeMapSlow<>(newRoot);
    }

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

    private Node<K, V> findMin(Node<K, V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<K, V> deleteMinCopy(final Node<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        return new Node<>(node.key, node.value,
                deleteMinCopy(node.left), node.right);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        List<Map.Entry<K, V>> out = new ArrayList<>();
        inorder(root, out);
        return out.iterator();
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

    @Override
    public int size() {
        return size(root);
    }

    private int size(final Node<K, V> node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left) + size(node.right);
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    // Immutable
    @Override
    public boolean add(final Map.Entry<K, V> e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends Map.Entry<K, V>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
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
    public boolean contains(final Object o) {
        if (!(o instanceof Map.Entry<?, ?>)) {
            return false;
        }
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        @SuppressWarnings("unchecked")
        K key = (K) e.getKey();
        @SuppressWarnings("unchecked")
        V val = (V) e.getValue();
        return Objects.equals(get(key), val);
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        int i = 0;
        for (Map.Entry<K, V> e : this) {
            arr[i++] = e;
        }
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(final E[] a) {
        List<Map.Entry<K, V>> list = new ArrayList<>();
        for (Map.Entry<K, V> e : this) {
            list.add(e);
        }
        if (a.length < list.size()) {
            return list.toArray((E[]) java.lang.reflect.Array
                    .newInstance(a.getClass()
                            .getComponentType(), list.size()
                    )
            );
        }
        for (int i = 0; i < list.size(); i++) {
            a[i] = (E) list.get(i);
        }
        if (a.length > list.size()) {
            a[list.size()] = null;
        }
        return a;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (Map.Entry<K, V> e : this) {
            result = prime * result + (e == null ? 0 : e.hashCode());
        }
        return result;
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<K, V>> it = iterator();
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Collection<?>)) {
            return false;
        }
        Collection<?> that = (Collection<?>) o;
        if (size() != that.size()) {
            return false;
        }
        Iterator<Map.Entry<K, V>> it1 = iterator();
        Iterator<?> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }
}
