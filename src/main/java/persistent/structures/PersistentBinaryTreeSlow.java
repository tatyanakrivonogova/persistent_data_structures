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
 * Persistent binary search tree that makes full deep copy on every modification.
 * Used for performance comparison with path-copying implementation.
 *
 * @param <T> the type of elements, must be Comparable
 */
public final class PersistentBinaryTreeSlow<T extends Comparable<T>>
    implements PersistentStructure<T> {

  /**
   * Mutable tree node for this implementation.
   * @param <T> type of element in node, should implement Comparable<T>
   */
  private static final class Node<T extends Comparable<T>> {
    private T value;
    private Node<T> left;
    private Node<T> right;
    private int height;
    private int size;

    /**
     * Constructs a new node with the given value and children.
     *
     * @param nodeValue the value to store
     * @param leftChild the left child
     * @param rightChild the right child
     */
    Node(final T nodeValue, final Node<T> leftChild,
        final Node<T> rightChild) {
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

    /**
     * Updates height and size based on children.
     */
    void updateMetadata() {
      this.height = 1 + Math.max(height(left), height(right));
      this.size = 1 + size(left) + size(right);
    }

    /**
     * Returns the height of a node, or 0 if null.
     *
     * @param <T> the type of node value
     * @param node the node to check
     * @return the height of the node
     */
    static <T extends Comparable<T>> int height(final Node<T> node) {
      return node == null ? 0 : node.height;
    }

    /**
     * Returns the size of a node's subtree, or 0 if null.
     *
     * @param <T> the type of node value
     * @param node the node to check
     * @return the size of the subtree
     */
    static <T extends Comparable<T>> int size(final Node<T> node) {
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

    /**
     * Makes a deep copy of this node and its entire subtree.
     *
     * @return deep copy of the node
     */
    Node<T> deepCopy() {
      return new Node<>(this);
    }
  }

  /** The root node of this tree. */
  private final Node<T> root;

  /** Public constructor for empty tree. */
  public PersistentBinaryTreeSlow() {
    this.root = null;
  }

  /**
   * Private constructor for internal use.
   * @param newRoot new root node
   */
  private PersistentBinaryTreeSlow(final Node<T> newRoot) {
    this.root = newRoot;
  }

  // ========== PersistentStructure interface implementations ==========

  @Override
  public PersistentStructure<T> createWithAdded(final T element) {
    if (element == null) {
      return this;
    }
    
    // Deep copy the entire tree
    Node<T> newRoot = root != null ? root.deepCopy() : null;
    
    // Insert into the copy
    newRoot = insertMutable(newRoot, element);
    
    return new PersistentBinaryTreeSlow<>(newRoot);
  }

  @Override
  public PersistentStructure<T> createWithRemoved(final T element) {
    if (element == null) {
      return this;
    }
    
    if (root == null) {
      return this; // Nothing to remove
    }
    
    // Deep copy the entire tree
    Node<T> newRoot = root.deepCopy();
    
    // Remove from the copy
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

  // ========== Collection interface methods ==========

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
  public <E> E[] toArray(final E[] a) {
    int size = size();
    E[] result = a;

    if (result.length < size) {
      result = (E[]) java.lang.reflect.Array.newInstance(
          a.getClass().getComponentType(), size);
    }

    int i = 0;
    for (T element : this) {
      result[i++] = (E) element;
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
    // For full copy implementation, we can return this since it's already immutable
    // But for true snapshot semantics, we should make a copy
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
    if (!(o instanceof Collection)) {
      return false;
    }

    Collection<?> that = (Collection<?>) o;
    if (size() != that.size()) {
      return false;
    }

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
    final int prime = 31;
    int result = 1;
    for (T element : this) {
      result = prime * result + (element == null ? 0 : element.hashCode());
    }
    return result;
  }

  @Override
  public String toString() {
    Iterator<T> it = iterator();
    if (!it.hasNext()) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (;;) {
      T e = it.next();
      sb.append(e);
      if (!it.hasNext()) {
        return sb.append(']').toString();
      }
      sb.append(',').append(' ');
    }
  }

  // ========== Tree operations (mutable, work on copied tree) ==========

  private boolean contains(final Node<T> node, final T value) {
    Node<T> currentNode = node;
    while (currentNode != null) {
      int cmp = value.compareTo(currentNode.value);
      if (cmp < 0) {
        currentNode = currentNode.left;
      } else if (cmp > 0) {
        currentNode = currentNode.right;
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Mutable insert - modifies the tree in place.
   * Assumes we're working on a fresh copy.
   */
  private Node<T> insertMutable(Node<T> node, final T value) {
    if (node == null) {
      return new Node<>(value, null, null);
    }

    int cmp = value.compareTo(node.value);
    if (cmp < 0) {
      node.left = insertMutable(node.left, value);
    } else if (cmp > 0) {
      node.right = insertMutable(node.right, value);
    } else {
      return node; // Value already exists
    }

    node.updateMetadata();
    return balanceMutable(node);
  }

  /**
   * Mutable delete - modifies the tree in place.
   * Assumes we're working on a fresh copy.
   */
  private Node<T> deleteMutable(Node<T> node, final T value) {
    if (node == null) {
      return null;
    }

    int cmp = value.compareTo(node.value);
    if (cmp < 0) {
      node.left = deleteMutable(node.left, value);
    } else if (cmp > 0) {
      node.right = deleteMutable(node.right, value);
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
      node.value = minNode.value;
      node.right = deleteMinMutable(node.right);
    }

    node.updateMetadata();
    return balanceMutable(node);
  }

  /**
   * Mutable balance - modifies the tree in place.
   */
  private Node<T> balanceMutable(final Node<T> node) {
    if (node == null) {
      return null;
    }

    int balanceFactor = node.balanceFactor();

    if (balanceFactor > 1) {
      if (node.left.balanceFactor() < 0) {
        node.left = rotateLeftMutable(node.left);
      }
      return rotateRightMutable(node);
    }

    if (balanceFactor < -1) {
      if (node.right.balanceFactor() > 0) {
        node.right = rotateRightMutable(node.right);
      }
      return rotateLeftMutable(node);
    }

    return node;
  }

  /**
   * Mutable right rotation.
   */
  private Node<T> rotateRightMutable(final Node<T> y) {
    Node<T> x = y.left;
    Node<T> t2 = x.right;

    // Perform rotation
    x.right = y;
    y.left = t2;

    // Update heights
    y.updateMetadata();
    x.updateMetadata();

    return x;
  }

  /**
   * Mutable left rotation.
   */
  private Node<T> rotateLeftMutable(final Node<T> x) {
    Node<T> y = x.right;
    Node<T> t2 = y.left;

    // Perform rotation
    y.left = x;
    x.right = t2;

    // Update heights
    x.updateMetadata();
    y.updateMetadata();

    return y;
  }

  private Node<T> findMin(final Node<T> node) {
    Node<T> currentNode = node;
    while (currentNode.left != null) {
      currentNode = currentNode.left;
    }
    return currentNode;
  }

  private Node<T> findMax(final Node<T> node) {
    Node<T> currentNode = node;
    while (currentNode.right != null) {
      currentNode = currentNode.right;
    }
    return currentNode;
  }

  /**
   * Mutable delete minimum.
   */
  private Node<T> deleteMinMutable(Node<T> node) {
    if (node.left == null) {
      return node.right;
    }
    
    node.left = deleteMinMutable(node.left);
    node.updateMetadata();
    return balanceMutable(node);
  }

  // ========== Iterator ==========

  private final class TreeIterator implements Iterator<T> {
    /** The elements in order. */
    private final List<T> elements;
    /** The current index in the iteration. */
    private int currentIndex;

    /** Creates a new iterator. */
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

  private void inOrderTraversal(final Node<T> node,
      final java.util.function.Consumer<T> action) {
    if (node != null) {
      inOrderTraversal(node.left, action);
      action.accept(node.value);
      inOrderTraversal(node.right, action);
    }
  }

  // ========== Utility methods ==========

  /**
   * Returns the minimum element in this tree.
   *
   * @return the minimum element
   * @throws NoSuchElementException if the tree is empty
   */
  public T min() {
    if (root == null) {
      throw new NoSuchElementException("Tree is empty");
    }
    return findMin(root).value;
  }

  /**
   * Returns the maximum element in this tree.
   *
   * @return the maximum element
   * @throws NoSuchElementException if the tree is empty
   */
  public T max() {
    if (root == null) {
      throw new NoSuchElementException("Tree is empty");
    }
    return findMax(root).value;
  }

  /**
   * Returns the height of this tree.
   *
   * @return the height of the tree
   */
  public int height() {
    return Node.height(root);
  }

  /**
   * Checks if this tree is balanced.
   *
   * @return true if the tree is balanced, false otherwise
   */
  public boolean isBalanced() {
    return isBalanced(root);
  }

  private boolean isBalanced(final Node<T> node) {
    if (node == null) {
      return true;
    }

    int balanceFactor = node.balanceFactor();
    if (Math.abs(balanceFactor) > 1) {
      return false;
    }

    return isBalanced(node.left) && isBalanced(node.right);
  }

  /**
   * Creates a deep copy of this tree.
   * For testing and comparison purposes.
   *
   * @return deep copy of the tree
   */
  public PersistentBinaryTreeSlow<T> deepCopy() {
    if (root == null) {
      return new PersistentBinaryTreeSlow<>(null);
    }
    return new PersistentBinaryTreeSlow<>(root.deepCopy());
  }
}