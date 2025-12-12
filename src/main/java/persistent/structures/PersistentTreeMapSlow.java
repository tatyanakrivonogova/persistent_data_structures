package persistent.structures;

import java.util.*;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

public final class PersistentTreeMapSlow<K extends Comparable<K>, V>
        implements PersistentStructure<Map.Entry<K, V>> {

    /** Узел обычного несбалансированного дерева. */
    private static final class Node<K, V> {
        final K key;
        final V value;
        final Node<K, V> left;
        final Node<K, V> right;

        Node(K key, V value, Node<K, V> left, Node<K, V> right) {
            this.key  = key;
            this.value = value;
            this.left  = left;
            this.right = right;
        }
    }

    /** Корень. */
    private final Node<K, V> root;

    /** Пустой map. */
    public PersistentTreeMapSlow() {
        this.root = null;
    }

    /** Внутренний конструктор. */
    private PersistentTreeMapSlow(Node<K, V> root) {
        this.root = root;
    }

    // =====================================================================
    // PersistentStructure
    // =====================================================================

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithAdded(
            Map.Entry<K, V> entry) {
        if (entry == null || entry.getKey() == null) {
            return this;
        }
        return put(entry.getKey(), entry.getValue());
    }

    @Override
    public PersistentStructure<Map.Entry<K, V>> createWithRemoved(
            Map.Entry<K, V> entry) {
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
    public boolean containsElement(Map.Entry<K, V> entry) {
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

    // =====================================================================
    // Основные операции map
    // =====================================================================

    public V get(K key) {
        Node<K,V> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) cur = cur.left;
            else if (cmp > 0) cur = cur.right;
            else return cur.value;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public boolean containsValue(V value) {
        return containsValue(root, value);
    }

    private boolean containsValue(Node<K,V> n, V value) {
        if (n == null) {
            return false;
        }
        if (Objects.equals(n.value, value)) {
            return true;
        }
        return containsValue(n.left, value) || containsValue(n.right, value);
    }

    /** Наивно копирует всё дерево + добавляет/обновляет ключ. */
    public PersistentTreeMapSlow<K,V> put(K key, V value) {
        V oldValue = get(key);
        if (Objects.equals(oldValue, value)) {
            return this;
        }
        Node<K,V> newRoot = putCopy(root, key, value);
        return new PersistentTreeMapSlow<>(newRoot);
    }

    private Node<K,V> putCopy(Node<K,V> node, K key, V value) {
        if (node == null) {
            return new Node<>(key, value, null, null);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return new Node<>(node.key, node.value,
                    putCopy(node.left, key, value), node.right);
        } else if (cmp > 0) {
            return new Node<>(node.key, node.value,
                    node.left, putCopy(node.right, key, value));
        } else {
            return new Node<>(key, value, node.left, node.right); // update
        }
    }

    /** Наивно копирует всё дерево + удаляет ключ. */
    public PersistentTreeMapSlow<K,V> remove(K key) {
        if (!containsKey(key)) {
            return this;
        }
        Node<K,V> newRoot = removeCopy(root, key);
        return new PersistentTreeMapSlow<>(newRoot);
    }

    private Node<K,V> removeCopy(Node<K,V> node, K key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            return new Node<>(
                    node.key, node.value,
                    removeCopy(node.left, key),
                    node.right
            );
        } else if (cmp > 0) {
            return new Node<>(
                    node.key, node.value,
                    node.left,
                    removeCopy(node.right, key)
            );
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            Node<K,V> min = findMin(node.right);
            Node<K,V> newRight = deleteMinCopy(node.right);

            return new Node<>(
                    min.key, min.value,
                    node.left,
                    newRight
            );
        }
    }

    private Node<K,V> findMin(Node<K,V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<K,V> deleteMinCopy(Node<K,V> node) {
        if (node.left == null) {
            return node.right;
        }
        return new Node<>(
                node.key, node.value,
                deleteMinCopy(node.left),
                node.right
        );
    }


    // =====================================================================
    // Collection interface via iterator
    // =====================================================================

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        List<Map.Entry<K,V>> list = new ArrayList<>();
        inorder(root, list);
        return list.iterator();
    }

    private void inorder(Node<K,V> n, List<Map.Entry<K,V>> out) {
        if (n == null) {
            return;
        }
        inorder(n.left, out);
        out.add(new AbstractMap.SimpleEntry<>(n.key, n.value));
        inorder(n.right, out);
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(Node<K,V> n) {
        if (n == null) {
            return 0;
        }
        return 1 + size(n.left) + size(n.right);
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    // Все методы мутабельности запрещены (immutable)
    @Override public boolean add(Map.Entry<K,V> e) {
        throw new UnsupportedOperationException();
    }
    @Override public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    @Override public boolean addAll(Collection<? extends Map.Entry<K,V>> c) {
        throw new UnsupportedOperationException();
    }
    @Override public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    @Override public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    @Override public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry<?,?>)) {
            return false;
        }
        Map.Entry<?,?> e = (Map.Entry<?,?>) o;
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
        for (Map.Entry<K,V> e : this) {
            arr[i++] = e;
        }
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        List<Map.Entry<K,V>> list = new ArrayList<>();
        for (Map.Entry<K,V> e : this) {
            list.add(e);
        }

        if (a.length < list.size()) {
            return list.toArray((E[]) java.lang.reflect.Array
                    .newInstance(a
                            .getClass()
                            .getComponentType(),
                            list.size()
                    ));
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
        for (Map.Entry<K, V> entry : this) {
            result = prime * result + (entry == null ? 0 : entry.hashCode());
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
}

