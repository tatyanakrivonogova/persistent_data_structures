package persistent.structures;

import java.util.*;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent, immutable doubly linked list implementation. Uses wrapper pattern for transactional
 * updates.
 *
 * @param <E> element type
 */
public final class PersistentDoublyLinkedList<E extends Comparable<E>>
    implements PersistentStructure<E> {

  /**
   * Doubly linked list node.
   *
   * @param <T> element type
   */
  private static final class Node<T> {

    /** Node value (immutable). */
    private final T value;

    /** Previous node. */
    private Node<T> prev;

    /** Next node. */
    private Node<T> next;

    Node(final T nodeValue, final Node<T> nodePrev, final Node<T> nodeNext) {
      this.value = nodeValue;
      this.prev = nodePrev;
      this.next = nodeNext;
    }

    public T getValue() {
      return value;
    }

    public Node<T> getPrev() {
      return prev;
    }

    public void setPrev(final Node<T> newPrev) {
      this.prev = newPrev;
    }

    public Node<T> getNext() {
      return next;
    }

    public void setNext(final Node<T> newNext) {
      this.next = newNext;
    }
  }

  /** First element. */
  private final Node<E> head;

  /** Last element. */
  private final Node<E> tail;

  /** Collection size. */
  private final int size;

  /**
   * Full constructor used internally.
   *
   * @param newHead list head
   * @param newTail list tail
   * @param newSize list size
   */
  private PersistentDoublyLinkedList(
      final Node<E> newHead, final Node<E> newTail, final int newSize) {
    this.head = newHead;
    this.tail = newTail;
    this.size = newSize;
  }

  /** Creates an empty persistent doubly linked list. */
  public PersistentDoublyLinkedList() {
    this.head = null;
    this.tail = null;
    this.size = 0;
  }

  // ========== PersistentStructure interface implementations ==========

  @Override
  public PersistentStructure<E> createWithAdded(E element) {
    return add(size, element);
  }

  @Override
  public PersistentStructure<E> createWithRemoved(E element) {
    Node<E> current = head;
    int index = 0;
    while (current != null) {
      if (Objects.equals(current.getValue(), element)) {
        return remove(index);
      }
      current = current.getNext();
      index++;
    }
    return this; // Element not found
  }

  @Override
  public PersistentStructure<E> createEmpty() {
    return new PersistentDoublyLinkedList<>();
  }

  @Override
  public boolean containsElement(E element) {
    Node<E> current = head;
    while (current != null) {
      if (Objects.equals(current.getValue(), element)) {
        return true;
      }
      current = current.getNext();
    }
    return false;
  }

  @Override
  public Version getVersion() {
    // Generate version based on content hash
    return new SimpleVersion();
  }

  @Override
  public PersistentStructure<E> snapshot() {
    return this; // Already immutable
  }

  // ========== Collection interface methods ==========

  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public boolean contains(Object o) {
    try {
      @SuppressWarnings("unchecked")
      E element = (E) o;
      return containsElement(element);
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private Node<E> cur = head;

      @Override
      public boolean hasNext() {
        return cur != null;
      }

      @Override
      public E next() {
        if (cur == null) {
          throw new NoSuchElementException();
        }
        final E val = cur.getValue();
        cur = cur.getNext();
        return val;
      }
    };
  }

  @Override
  public Object[] toArray() {
    Object[] array = new Object[size];
    int i = 0;
    for (E element : this) {
      array[i++] = element;
    }
    return array;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    if (a.length < size) {
      a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
    }

    int i = 0;
    for (E element : this) {
      a[i++] = (T) element;
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
  public boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use TransactionalPersistentList wrapper for"
            + " mutable operations.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Collection)) return false;

    Collection<?> that = (Collection<?>) o;
    if (size() != that.size()) return false;

    Iterator<E> it1 = iterator();
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
    for (E element : this) {
      result = 31 * result + (element == null ? 0 : element.hashCode());
    }
    return result;
  }

  // ========== List-specific methods ==========

  /**
   * Checks if the list is empty.
   *
   * @return true if the list is empty
   */
  public boolean isEmptyList() {
    return size == 0;
  }

  /**
   * Returns element by index.
   *
   * @param index index of element
   * @return element value
   */
  public E get(final int index) {
    checkIndex(index);
    return nodeAt(index).getValue();
  }

  /**
   * Adds value to the beginning of the list.
   *
   * @param value new value
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> addFirst(final E value) {
    return add(0, value);
  }

  /**
   * Adds value to the end of the list.
   *
   * @param value new value
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> addLast(final E value) {
    return add(size, value);
  }

  /**
   * Inserts a value at a given index.
   *
   * @param index target index
   * @param value element to add
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> add(final int index, final E value) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException("index: " + index);
    }

    if (size == 0) {
      final Node<E> newNode = new Node<>(value, null, null);
      return new PersistentDoublyLinkedList<>(newNode, newNode, 1);
    }

    @SuppressWarnings("unchecked")
    Node<E>[] newHeadRef = new Node[] {null};
    Node<E> prevNewNode = null;
    Node<E> insertedNode = null;

    Node<E> curNode = head;
    int pos = 0;

    while (curNode != null) {
      if (pos == index) {
        insertedNode = new Node<>(value, prevNewNode, null);
        if (prevNewNode == null) {
          newHeadRef[0] = insertedNode;
        } else {
          prevNewNode.setNext(insertedNode);
        }
        insertedNode.setPrev(prevNewNode);
        prevNewNode = insertedNode;
      }

      prevNewNode = copyNode(curNode, newHeadRef, prevNewNode);

      curNode = curNode.getNext();
      pos++;
    }

    if (index == size) {
      final Node<E> lastNode = new Node<>(value, prevNewNode, null);
      if (prevNewNode == null) {
        newHeadRef[0] = lastNode;
      } else {
        prevNewNode.setNext(lastNode);
      }
      lastNode.setPrev(prevNewNode);
      prevNewNode = lastNode;
    }

    return new PersistentDoublyLinkedList<>(newHeadRef[0], prevNewNode, size + 1);
  }

  /**
   * Removes the first element.
   *
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> removeFirst() {
    if (size == 0) {
      throw new NoSuchElementException("removeFirst from empty list");
    }
    return remove(0);
  }

  /**
   * Removes the last element.
   *
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> removeLast() {
    if (size == 0) {
      throw new NoSuchElementException("removeLast from empty list");
    }
    return remove(size - 1);
  }

  /**
   * Removes element at index.
   *
   * @param index index of removable element
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> remove(final int index) {
    checkIndex(index);

    if (size == 1) {
      return new PersistentDoublyLinkedList<>(null, null, 0);
    }

    @SuppressWarnings("unchecked")
    Node<E>[] newHeadRef = new Node[] {null};
    Node<E> prevNewNode = null;

    Node<E> curNode = head;
    int pos = 0;

    while (curNode != null) {
      if (pos == index) {
        curNode = curNode.getNext();
        pos++;
        continue;
      }

      prevNewNode = copyNode(curNode, newHeadRef, prevNewNode);

      curNode = curNode.getNext();
      pos++;
    }

    return new PersistentDoublyLinkedList<>(newHeadRef[0], prevNewNode, size - 1);
  }

  /**
   * Creates a copy of a node, links it to the previous one and updates headRef.
   *
   * @param source original node
   * @param headRef reference to newHead
   * @param prevNew previous new node
   * @return newly created copied node
   */
  private Node<E> copyNode(final Node<E> source, final Node<E>[] headRef, final Node<E> prevNew) {
    final Node<E> copied = new Node<>(source.getValue(), prevNew, null);
    if (prevNew == null) {
      headRef[0] = copied;
    } else {
      prevNew.setNext(copied);
    }
    copied.setPrev(prevNew);
    return copied;
  }

  /**
   * Get node at index.
   *
   * @param index node index
   * @return node at index
   */
  private Node<E> nodeAt(final int index) {
    Node<E> current;

    if (index < (size >> 1)) {
      current = head;
      for (int i = 0; i < index; i++) {
        current = current.getNext();
      }
    } else {
      current = tail;
      for (int i = size - 1; i > index; i--) {
        current = current.getPrev();
      }
    }

    return current;
  }

  /**
   * Validates index.
   *
   * @param index index to validate
   */
  private void checkIndex(final int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
    }
  }

  /**
   * Returns string representation.
   *
   * @return string representation
   */
  @Override
  public String toString() {
    if (isEmpty()) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    sb.append('[');

    Node<E> curNode = head;
    while (curNode != null) {
      sb.append(curNode.getValue());
      curNode = curNode.getNext();
      if (curNode != null) {
        sb.append(", ");
      }
    }

    sb.append(']');
    return sb.toString();
  }
}
