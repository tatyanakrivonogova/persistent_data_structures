package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Persistent, immutable doubly linked list implementation.
 *
 * @param <E> element type
 */
public final class PersistentDoublyLinkedList<E extends Comparable<E>>
    implements PersistentStructure<E>, List<E> {

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
  public PersistentStructure<E> createWithAdded(final E element) {
    return addInternal(size, element);
  }

  @Override
  public PersistentStructure<E> createWithRemoved(final E element) {
    Node<E> current = head;
    int index = 0;
    while (current != null) {
      if (Objects.equals(current.getValue(), element)) {
        return removeInternal(index);
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
  public boolean containsElement(final E element) {
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

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  // ========== List interface methods (immutable ones) ==========

  @Override
  public boolean contains(final Object o) {
    try {
      @SuppressWarnings("unchecked")
      E element = (E) o;
      return containsElement(element);
    } catch (ClassCastException e) {
      return false;
    }
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
  public <T> T[] toArray(final T[] a) {
    T[] result = a;
    if (result.length < size) {
      result = (T[]) java.lang.reflect.Array.newInstance(
          a.getClass().getComponentType(), size);
    }

    int i = 0;
    for (E element : this) {
      result[i++] = (T) element;
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
  public E get(final int index) {
    checkIndex(index);
    return nodeAt(index).getValue();
  }

  @Override
  public int indexOf(final Object o) {
    try {
      @SuppressWarnings("unchecked")
      E element = (E) o;
      Node<E> current = head;
      int index = 0;
      while (current != null) {
        if (Objects.equals(current.getValue(), element)) {
          return index;
        }
        current = current.getNext();
        index++;
      }
      return -1;
    } catch (ClassCastException e) {
      return -1;
    }
  }

  @Override
  public int lastIndexOf(final Object o) {
    try {
      @SuppressWarnings("unchecked")
      E element = (E) o;
      Node<E> current = tail;
      int index = size - 1;
      while (current != null) {
        if (Objects.equals(current.getValue(), element)) {
          return index;
        }
        current = current.getPrev();
        index--;
      }
      return -1;
    } catch (ClassCastException e) {
      return -1;
    }
  }

  @Override
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<E> listIterator(final int index) {
    checkIndexForIterator(index);

    return new ListIterator<E>() {
      private Node<E> nextNode = (index == size) ? null : nodeAt(index);
      private Node<E> lastReturned = null;
      private int nextIndex = index;

      @Override
      public boolean hasNext() {
        return nextIndex < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        lastReturned = nextNode;
        nextNode = nextNode.getNext();
        nextIndex++;
        return lastReturned.getValue();
      }

      @Override
      public boolean hasPrevious() {
        return nextIndex > 0;
      }

      @Override
      public E previous() {
        if (!hasPrevious()) {
          throw new NoSuchElementException();
        }
        nextNode = (nextNode == null) ? tail : nextNode.getPrev();
        lastReturned = nextNode;
        nextIndex--;
        return lastReturned.getValue();
      }

      @Override
      public int nextIndex() {
        return nextIndex;
      }

      @Override
      public int previousIndex() {
        return nextIndex - 1;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException(
            "PersistentDoublyLinkedList is immutable");
      }

      @Override
      public void set(final E e) {
        throw new UnsupportedOperationException(
            "PersistentDoublyLinkedList is immutable");
      }

      @Override
      public void add(final E e) {
        throw new UnsupportedOperationException(
            "PersistentDoublyLinkedList is immutable");
      }
    };
  }

  @Override
  public List<E> subList(final int fromIndex, final int toIndex) {
    if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException();
    }

    if (fromIndex == toIndex) {
      return new PersistentDoublyLinkedList<>();
    }

    PersistentDoublyLinkedList<E> result = new PersistentDoublyLinkedList<>();
    Node<E> current = nodeAt(fromIndex);
    for (int i = fromIndex; i < toIndex; i++) {
      result = result.addLastInternal(current.getValue());
      current = current.getNext();
    }

    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof List)) {
      return false;
    }

    List<?> that = (List<?>) o;
    if (size() != that.size()) {
      return false;
    }

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
    final int prime = 31;
    int result = 1;
    for (E element : this) {
      result = prime * result + (element == null ? 0 : element.hashCode());
    }
    return result;
  }

  // ========== Mutable List interface methods ==========

  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public boolean addAll(final Collection<? extends E> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends E> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public E set(final int index, final E element) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public void add(final int index, final E element) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  @Override
  public E remove(final int index) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  /**
   * Adds element to the beginning (mutable version).
   *
   * @param e element to add
   */
  public void addFirst(final E e) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  /**
   * Adds element to the end (mutable version).
   *
   * @param e element to add
   */
  public void addLast(final E e) {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  /**
   * Removes first element (mutable version).
   *
   * @return removed element
   */
  public E removeFirst() {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  /**
   * Removes last element (mutable version).
   *
   * @return removed element
   */
  public E removeLast() {
    throw new UnsupportedOperationException(
        "PersistentDoublyLinkedList is immutable. Use "
            + "TransactionalPersistentDoublyLinkedList for mutable operations.");
  }

  // ========== Persistent list-specific methods ==========

  /**
   * Adds value to the beginning of the list (persistent version).
   *
   * @param value new value
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> addFirstInternal(final E value) {
    return addInternal(0, value);
  }

  /**
   * Adds value to the end of the list (persistent version).
   *
   * @param value new value
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> addLastInternal(final E value) {
    return addInternal(size, value);
  }

  /**
   * Inserts a value at a given index (persistent version).
   *
   * @param index target index
   * @param value element to add
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> addInternal(final int index,
      final E value) {
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

    return new PersistentDoublyLinkedList<>(
        newHeadRef[0], prevNewNode, size + 1);
  }

  /**
   * Removes the first element (persistent version).
   *
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> removeFirstInternal() {
    if (size == 0) {
      throw new NoSuchElementException("removeFirst from empty list");
    }
    return removeInternal(0);
  }

  /**
   * Removes the last element (persistent version).
   *
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> removeLastInternal() {
    if (size == 0) {
      throw new NoSuchElementException("removeLast from empty list");
    }
    return removeInternal(size - 1);
  }

  /**
   * Removes element at index (persistent version).
   *
   * @param index index of removable element
   * @return new list version
   */
  public PersistentDoublyLinkedList<E> removeInternal(final int index) {
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

    return new PersistentDoublyLinkedList<>(
        newHeadRef[0], prevNewNode, size - 1);
  }

  // ========== Helper methods ==========

  /**
   * Creates a copy of a node, links it to the previous one and updates
   * headRef.
   *
   * @param source original node
   * @param headRef reference to newHead
   * @param prevNew previous new node
   * @return newly created copied node
   */
  private Node<E> copyNode(final Node<E> source, final Node<E>[] headRef,
      final Node<E> prevNew) {
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
      throw new IndexOutOfBoundsException(
          "index: " + index + ", size: " + size);
    }
  }

  /**
   * Validates index for iterator creation.
   *
   * @param index index to validate
   */
  private void checkIndexForIterator(final int index) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(
          "index: " + index + ", size: " + size);
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
