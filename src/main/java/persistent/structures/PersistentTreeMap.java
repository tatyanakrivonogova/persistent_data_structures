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
 * Persistent tree map that fully implements Map interface. Uses wrapper
 * pattern for transactional updates.
 *
 * @param <K> the type of keys, must be Comparable
 * @param <V> the type of values
 */
public final class PersistentTreeMap<K extends Comparable<K>, V>
    implements PersistentStructure<Map.Entry<K, V>>, Map<K, V> {

  /**
    * Immutable tree node.
    * @param <K> the type of keys, must implement Comparable<K>
    * @param <V> the type of mapped values
    */
  private static final class TreeNode<K extends Comparable<K>, V> {
    /** The key stored in this node. */
    private final K key;
    /** The value stored in this node. */
    private final V value;
    /** The left child node. */
    private final TreeNode<K, V> left;
    /** The right child node. */
    private final TreeNode<K, V> right;
    /** The height of this node. */
    private final int height;
    /** The size of the subtree rooted at this node. */
    private final int size;

    /**
     * Constructs a new tree node.
     *
     * @param nodeKey the key
     * @param nodeValue the value
     * @param leftChild the left child
     * @param rightChild the right child
     */
    TreeNode(final K nodeKey, final V nodeValue,
        final TreeNode<K, V> leftChild, final TreeNode<K, V> rightChild) {
      this.key = nodeKey;
      this.value = nodeValue;
      this.left = leftChild;
      this.right = rightChild;
      this.height = 1 + Math.max(height(leftChild), height(rightChild));
      this.size = 1 + size(leftChild) + size(rightChild);
    }

    /**
     * Returns the height of a node, or 0 if null.
     *
     * @param <K> the type of keys
     * @param <V> the type of values
     * @param node the node to check
     * @return the height of the node
     */
    static <K extends Comparable<K>, V> int height(
        final TreeNode<K, V> node) {
      return node == null ? 0 : node.height;
    }

    /**
     * Returns the size of a node's subtree, or 0 if null.
     *
     * @param <K> the type of keys
     * @param <V> the type of values
     * @param node the node to check
     * @return the size of the subtree
     */
    static <K extends Comparable<K>, V> int size(
        final TreeNode<K, V> node) {
      return node == null ? 0 : node.size;
    }

    /**
     * Returns the balance factor of this node.
     *
     * @return height(left) - height(right)
     */
    int balanceFactor() {
      return height(left) - height(right);
    }
  }

  /** The root node of this tree map. */
  private final TreeNode<K, V> root;

  /** Public constructor for empty map. */
  public PersistentTreeMap() {
    this.root = null;
  }

  /**
   * Private constructor for internal use.
   * @param newRoot new root node
   */
  private PersistentTreeMap(final TreeNode<K, V> newRoot) {
    this.root = newRoot;
  }

  // ========== PersistentStructure interface implementations ==========

  @Override
  public PersistentStructure<Map.Entry<K, V>> createWithAdded(
      final Map.Entry<K, V> entry) {
    if (entry == null || entry.getKey() == null) {
      return this; // Don't add null entries or null keys
    }
    // Приведение типа, так как putInternal возвращает PersistentTreeMap<K, V>
    return (PersistentStructure<Map.Entry<K, V>>) putInternal(entry.getKey(), entry.getValue());
  }

  @Override
  public PersistentStructure<Map.Entry<K, V>> createWithRemoved(
      final Map.Entry<K, V> entry) {
    if (entry == null || entry.getKey() == null) {
      return this; // Can't remove null
    }
    // Приведение типа, так как removeInternal возвращает PersistentTreeMap<K, V>
    return (PersistentStructure<Map.Entry<K, V>>) removeInternal(entry.getKey());
  }

  @Override
  public PersistentStructure<Map.Entry<K, V>> createEmpty() {
    return new PersistentTreeMap<>(null);
  }

  @Override
  public boolean containsElement(final Map.Entry<K, V> entry) {
    if (entry == null || entry.getKey() == null) {
      return false;
    }
    V value = get(entry.getKey());
    return value != null && value.equals(entry.getValue());
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
  public Version getVersion() {
    return new SimpleVersion();
  }

  @Override
  public PersistentStructure<Map.Entry<K, V>> snapshot() {
    return this; // Already immutable
  }

  // ========== Map interface methods (immutable ones) ==========

  @Override
  public boolean containsKey(final Object key) {
    try {
      @SuppressWarnings("unchecked")
      K k = (K) key;
      return get(k) != null;
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
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap for mutable operations.");
  }

  // ========== Map interface methods (throw UnsupportedOperationException) ==========

  @Override
  public V put(final K key, final V value) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap for mutable operations.");
  }

  @Override
  public V remove(final Object key) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap for mutable operations.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap for mutable operations.");
  }

  // ========== Persistent map-specific methods ==========

  /**
   * Associates the specified value with the specified key (persistent version).
   *
   * @param key the key
   * @param value the value
   * @return new map version with the key-value pair added or updated
   */
  public PersistentTreeMap<K, V> putInternal(final K key, final V value) {
    TreeNode<K, V> newRoot = put(root, key, value);
    if (newRoot == root && getInternal(key) != null
        && Objects.equals(getInternal(key), value)) {
      return this; // No change
    }
    return new PersistentTreeMap<>(newRoot);
  }

  /**
   * Removes the mapping for the specified key (persistent version).
   *
   * @param key the key to remove
   * @return new map version with the key removed
   */
  public PersistentTreeMap<K, V> removeInternal(final K key) {
    if (!containsKey(key)) {
      return this;
    }
    TreeNode<K, V> newRoot = delete(root, key);
    return new PersistentTreeMap<>(newRoot == null ? null : newRoot);
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
    TreeNode<K, V> node = get(root, key);
    return node == null ? null : node.value;
  }

  /**
   * Returns the first (lowest) key.
   *
   * @return the first key
   * @throws NoSuchElementException if the map is empty
   */
  public K firstKey() {
    if (root == null) {
      throw new NoSuchElementException("Map is empty");
    }
    return findMin(root).key;
  }

  /**
   * Returns the last (highest) key.
   *
   * @return the last key
   * @throws NoSuchElementException if the map is empty
   */
  public K lastKey() {
    if (root == null) {
      throw new NoSuchElementException("Map is empty");
    }
    return findMax(root).key;
  }

  // ========== Tree operations ==========

  private TreeNode<K, V> get(final TreeNode<K, V> node, final K key) {
    if (key == null || node == null) {
      return null;
    }

    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
      return get(node.left, key);
    } else if (cmp > 0) {
      return get(node.right, key);
    } else {
      return node;
    }
  }

  private TreeNode<K, V> put(final TreeNode<K, V> node, final K key,
      final V value) {
    if (key == null) {
      return node; // Don't insert null key
    }

    if (node == null) {
      return new TreeNode<>(key, value, null, null);
    }

    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
      TreeNode<K, V> newLeft = put(node.left, key, value);
      if (newLeft == node.left) {
        // Check if we're updating existing key with same value
        TreeNode<K, V> existingLeft = get(node, key);
        if (existingLeft != null
          && Objects.equals(existingLeft.value, value)) {
          return node; // No change
        }
      }
      return balance(new TreeNode<>(node.key, node.value,
          newLeft, node.right));
    } else if (cmp > 0) {
      TreeNode<K, V> newRight = put(node.right, key, value);
      if (newRight == node.right) {
        // Check if we're updating existing key with same value
        TreeNode<K, V> existingRight = get(node, key);
        if (existingRight != null
          && Objects.equals(existingRight.value, value)) {
          return node; // No change
        }
      }
      return balance(new TreeNode<>(node.key, node.value,
          node.left, newRight));
    } else {
      // Update existing key
      if (Objects.equals(node.value, value)) {
        return node; // Same value, no change
      }
      return new TreeNode<>(key, value, node.left, node.right);
    }
  }

  private TreeNode<K, V> delete(final TreeNode<K, V> node, final K key) {
    if (key == null || node == null) {
      return node;
    }

    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
      TreeNode<K, V> newLeft = delete(node.left, key);
      if (newLeft == node.left) {
        return node;
      }
      return balance(new TreeNode<>(node.key, node.value,
          newLeft, node.right));
    } else if (cmp > 0) {
      TreeNode<K, V> newRight = delete(node.right, key);
      if (newRight == node.right) {
        return node;
      }
      return balance(new TreeNode<>(node.key, node.value,
          node.left, newRight));
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
      return balance(new TreeNode<>(minNode.key, minNode.value,
          node.left, newRight));
    }
  }

  private boolean containsValue(final TreeNode<K, V> node, final V value) {
    if (node == null) {
      return false;
    }

    if (Objects.equals(value, node.value)) {
      return true;
    }

    return containsValue(node.left, value)
        || containsValue(node.right, value);
  }

  private TreeNode<K, V> balance(final TreeNode<K, V> node) {
    if (node == null) {
      return null;
    }

    int balanceFactor = node.balanceFactor();

    if (balanceFactor > 1) {
      if (node.left.balanceFactor() < 0) {
        // Left-Right case
        return rotateRight(new TreeNode<>(node.key, node.value,
            rotateLeft(node.left), node.right));
      }
      // Left-Left case
      return rotateRight(node);
    }

    if (balanceFactor < -1) {
      if (node.right.balanceFactor() > 0) {
        // Right-Left case
        return rotateLeft(new TreeNode<>(node.key, node.value,
            node.left, rotateRight(node.right)));
      }
      // Right-Right case
      return rotateLeft(node);
    }

    return node;
  }

  private TreeNode<K, V> rotateRight(final TreeNode<K, V> y) {
    TreeNode<K, V> x = y.left;
    TreeNode<K, V> t2 = x.right;

    return new TreeNode<>(x.key, x.value, x.left,
        new TreeNode<>(y.key, y.value, t2, y.right));
  }

  private TreeNode<K, V> rotateLeft(final TreeNode<K, V> x) {
    TreeNode<K, V> y = x.right;
    TreeNode<K, V> t2 = y.left;

    return new TreeNode<>(y.key, y.value,
        new TreeNode<>(x.key, x.value, x.left, t2), y.right);
  }

  private TreeNode<K, V> findMin(final TreeNode<K, V> node) {
    TreeNode<K, V> currentNode = node;
    while (currentNode.left != null) {
      currentNode = currentNode.left;
    }
    return currentNode;
  }

  private TreeNode<K, V> findMax(final TreeNode<K, V> node) {
    TreeNode<K, V> currentNode = node;
    while (currentNode.right != null) {
      currentNode = currentNode.right;
    }
    return currentNode;
  }

  private TreeNode<K, V> deleteMin(final TreeNode<K, V> node) {
    if (node.left == null) {
      return node.right;
    }
    return balance(new TreeNode<>(node.key, node.value,
        deleteMin(node.left), node.right));
  }

  private boolean isBalanced(final TreeNode<K, V> node) {
    if (node == null) {
      return true;
    }

    int balanceFactor = node.balanceFactor();
    if (Math.abs(balanceFactor) > 1) {
      return false;
    }

    return isBalanced(node.left) && isBalanced(node.right);
  }

  // ========== EntrySet, KeySet, and Values implementations ==========

  private final class EntrySet implements Set<Map.Entry<K, V>> {
    @Override
    public int size() {
      return PersistentTreeMap.this.size();
    }

    @Override
    public boolean isEmpty() {
      return PersistentTreeMap.this.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      V value = PersistentTreeMap.this.get(entry.getKey());
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean remove(final Object o) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
      return PersistentTreeMap.this.size();
    }

    @Override
    public boolean isEmpty() {
      return PersistentTreeMap.this.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      return PersistentTreeMap.this.containsKey(o);
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean remove(final Object o) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
      return PersistentTreeMap.this.size();
    }

    @Override
    public boolean isEmpty() {
      return PersistentTreeMap.this.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      return PersistentTreeMap.this.containsValue(o);
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean remove(final Object o) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException(
          "PersistentTreeMap is immutable");
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
      inOrderTraversal(root, elements);
    }
  }

  private final class KeyIterator extends TreeIterator<K> {
    @Override
    void fillElements() {
      inOrderTraversalKeys(root, elements);
    }
  }

  private final class ValueIterator extends TreeIterator<V> {
    @Override
    void fillElements() {
      inOrderTraversalValues(root, elements);
    }
  }

  private void inOrderTraversal(final TreeNode<K, V> node,
      final List<Map.Entry<K, V>> result) {
    if (node != null) {
      inOrderTraversal(node.left, result);
      result.add(new AbstractMap.SimpleEntry<>(node.key, node.value));
      inOrderTraversal(node.right, result);
    }
  }

  private void inOrderTraversalKeys(final TreeNode<K, V> node,
      final List<K> result) {
    if (node != null) {
      inOrderTraversalKeys(node.left, result);
      result.add(node.key);
      inOrderTraversalKeys(node.right, result);
    }
  }

  private void inOrderTraversalValues(final TreeNode<K, V> node,
      final List<V> result) {
    if (node != null) {
      inOrderTraversalValues(node.left, result);
      result.add(node.value);
      inOrderTraversalValues(node.right, result);
    }
  }

  // ========== Utility methods ==========

  /**
   * Returns the height of this tree map.
   *
   * @return the height of the tree
   */
  public int height() {
    return TreeNode.height(root);
  }

  /**
   * Checks if this tree map is balanced.
   *
   * @return true if the tree is balanced, false otherwise
   */
  public boolean isBalanced() {
    return isBalanced(root);
  }

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
