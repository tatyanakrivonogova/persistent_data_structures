package persistent.structures;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent tree map that fully implements Collection interface. Uses wrapper
 * pattern for transactional updates.
 *
 * @param <K> the type of keys, must be Comparable
 * @param <V> the type of values
 */
public final class PersistentTreeMap<K extends Comparable<K>, V>
    implements PersistentStructure<Map.Entry<K, V>> {

  /** Immutable tree node. */
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

  /** Private constructor for internal use. */
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
    return put(entry.getKey(), entry.getValue());
  }

  @Override
  public PersistentStructure<Map.Entry<K, V>> createWithRemoved(
      final Map.Entry<K, V> entry) {
    if (entry == null || entry.getKey() == null) {
      return this; // Can't remove null
    }
    return remove(entry.getKey());
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

  // ========== Map-specific operations ==========

  /**
   * Associates the specified value with the specified key.
   *
   * @param key the key
   * @param value the value
   * @return new map version with the key-value pair added or updated
   */
  public PersistentTreeMap<K, V> put(final K key, final V value) {
    TreeNode<K, V> newRoot = put(root, key, value);
    if (newRoot == root && get(key) != null
        && Objects.equals(get(key), value)) {
      return this; // No change
    }
    return new PersistentTreeMap<>(newRoot);
  }

  /**
   * Removes the mapping for the specified key.
   *
   * @param key the key to remove
   * @return new map version with the key removed
   */
  public PersistentTreeMap<K, V> remove(final K key) {
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
  public V get(final K key) {
    if (key == null) {
      return null;
    }
    TreeNode<K, V> node = get(root, key);
    return node == null ? null : node.value;
  }

  /**
   * Returns true if this map contains the specified key.
   *
   * @param key the key
   * @return true if the map contains the key
   */
  public boolean containsKey(final K key) {
    return get(key) != null;
  }

  /**
   * Returns true if this map contains the specified value.
   *
   * @param value the value
   * @return true if the map contains the value
   */
  public boolean containsValue(final V value) {
    return containsValue(root, value);
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

  // ========== Collection interface methods ==========

  @Override
  public boolean add(final Map.Entry<K, V> entry) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
  }

  @Override
  public boolean contains(final Object o) {
    if (o == null) {
      return false;
    }
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
  public <E> E[] toArray(final E[] a) {
    E[] result = a;
    int size = size();
    if (result.length < size) {
      result = (E[]) java.lang.reflect.Array.newInstance(
          a.getClass().getComponentType(), size);
    }

    int i = 0;
    for (Map.Entry<K, V> entry : this) {
      result[i++] = (E) entry;
    }

    if (result.length > size) {
      result[size] = null;
    }

    return result;
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
  public boolean addAll(final Collection<? extends Map.Entry<K, V>> c) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "PersistentTreeMap is immutable. Use "
            + "TransactionalPersistentTreeMap wrapper for mutable operations.");
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
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Collection)) {
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
        TreeNode<K, V> existing = get(node, key);
        if (existing != null && Objects.equals(existing.value, value)) {
          return node; // No change
        }
      }
      return balance(new TreeNode<>(node.key, node.value,
          newLeft, node.right));
    } else if (cmp > 0) {
      TreeNode<K, V> newRight = put(node.right, key, value);
      if (newRight == node.right) {
        // Check if we're updating existing key with same value
        TreeNode<K, V> existing = get(node, key);
        if (existing != null && Objects.equals(existing.value, value)) {
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

  // ========== Iterator ==========

  private final class MapIterator implements Iterator<Map.Entry<K, V>> {
    /** The entries in order. */
    private final List<Map.Entry<K, V>> entries;
    /** The current index in the iteration. */
    private int currentIndex;

    /** Creates a new iterator. */
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

  private void inOrderTraversal(final TreeNode<K, V> node,
      final List<Map.Entry<K, V>> result) {
    if (node != null) {
      inOrderTraversal(node.left, result);
      result.add(new AbstractMap.SimpleEntry<>(node.key, node.value));
      inOrderTraversal(node.right, result);
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
}