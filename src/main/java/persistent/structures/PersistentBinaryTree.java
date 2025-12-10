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
 * Persistent binary search tree that fully implements Collection interface.
 * Uses wrapper pattern for transactional updates.
 *
 * @param <T> the type of elements, must be Comparable
 */
public final class PersistentBinaryTree<T extends Comparable<T>>
    implements PersistentStructure<T> {

  /** Immutable tree node. */
  private static final class Node<T extends Comparable<T>> {
    /** The value stored in this node. */
    private final T value;
    /** The left child node. */
    private final Node<T> left;
    /** The right child node. */
    private final Node<T> right;
    /** The height of this node. */
    private final int height;
    /** The size of the subtree rooted at this node. */
    private final int size;

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
      this.height = 1 + Math.max(height(leftChild), height(rightChild));
      this.size = 1 + size(leftChild) + size(rightChild);
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
  }

  /** The root node of this tree. */
  private final Node<T> root;

  /** Public constructor for empty tree. */
  public PersistentBinaryTree() {
    this.root = null;
  }

  /** Private constructor for internal use. */
  private PersistentBinaryTree(final Node<T> newRoot) {
    this.root = newRoot;
  }

  // ========== PersistentStructure interface implementations ==========

  @Override
  public PersistentStructure<T> createWithAdded(final T element) {
    Node<T> newRoot = insert(root, element);
    if (newRoot == root) {
      return this; // Element already exists
    }
    return new PersistentBinaryTree<>(newRoot);
  }

  @Override
  public PersistentStructure<T> createWithRemoved(final T element) {
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
  public boolean containsElement(final T element) {
    if (element == null) {
      return false;
    }
    return contains(root, element);
  }

  // ========== Collection interface methods ==========

  @Override
  public boolean add(final T e) {
    // For PersistentBinaryTree, add() should throw since we're immutable
    // Users should use transactional wrapper
    throw new UnsupportedOperationException(
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException(
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
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
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "PersistentBinaryTree is immutable. Use "
            + "TransactionalPersistentTree wrapper for mutable operations.");
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

  // ========== Tree operations ==========

  private boolean contains(final Node<T> node, final T value) {
    if (value == null) {
      return false;
    }

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

  private Node<T> insert(final Node<T> node, final T value) {
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

  private Node<T> delete(final Node<T> node, final T value) {
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

  private Node<T> balance(final Node<T> node) {
    if (node == null) {
      return null;
    }

    int balanceFactor = node.balanceFactor();

    if (balanceFactor > 1) {
      if (node.left.balanceFactor() < 0) {
        // Left-Right case
        return rotateRight(new Node<>(node.value,
            rotateLeft(node.left), node.right));
      }
      // Left-Left case
      return rotateRight(node);
    }

    if (balanceFactor < -1) {
      if (node.right.balanceFactor() > 0) {
        // Right-Left case
        return rotateLeft(new Node<>(node.value,
            node.left, rotateRight(node.right)));
      }
      // Right-Right case
      return rotateLeft(node);
    }

    return node;
  }

  private Node<T> rotateRight(final Node<T> y) {
    Node<T> x = y.left;
    Node<T> t2 = x.right;

    return new Node<>(x.value, x.left,
        new Node<>(y.value, t2, y.right));
  }

  private Node<T> rotateLeft(final Node<T> x) {
    Node<T> y = x.right;
    Node<T> t2 = y.left;

    return new Node<>(y.value,
        new Node<>(x.value, x.left, t2), y.right);
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

  private Node<T> deleteMin(final Node<T> node) {
    if (node.left == null) {
      return node.right;
    }
    return balance(new Node<>(node.value,
        deleteMin(node.left), node.right));
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
}