package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Persistent, immutable doubly linked list implementation.
 *
 * @param <E> element type
 */
public final class PersistentDoublyLinkedList<E extends Comparable<E>>
        extends AbstractPersistentStructure<E> {

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
     * @param version structure version
     */
    private PersistentDoublyLinkedList(
            final Node<E> newHead,
            final Node<E> newTail,
            final int newSize,
            final Version version
    ) {
        super(version);
        this.head = newHead;
        this.tail = newTail;
        this.size = newSize;
    }

    /**
     * Creates an empty persistent doubly linked list.
     */
    public PersistentDoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Checks if the list is empty.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns list size.
     *
     * @return list size
     */
    public int size() {
        return size;
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

        final Version version = createNewVersion();

        if (size == 0) {
            final Node<E> newNode = new Node<>(value, null, null);
            return new PersistentDoublyLinkedList<>(newNode, newNode, 1, version);
        }

        Node<E> newHead = null;
        Node<E> prevNewNode = null;
        Node<E> insertedNode = null;

        Node<E> curNode = head;
        int pos = 0;

        while (curNode != null) {
            if (pos == index) {
                insertedNode = new Node<>(value, prevNewNode, null);
                if (prevNewNode == null) {
                    newHead = insertedNode;
                } else {
                    prevNewNode.setNext(insertedNode);
                }
                prevNewNode = insertedNode;
            }

            final Node<E> copied = new Node<>(curNode.getValue(), prevNewNode, null);
            if (prevNewNode == null) {
                newHead = copied;
            } else {
                prevNewNode.setNext(copied);
            }

            copied.setPrev(prevNewNode);

            prevNewNode = copied;
            curNode = curNode.getNext();
            pos++;
        }

        if (index == size) {
            final Node<E> lastNode = new Node<>(value, prevNewNode, null);
            if (prevNewNode == null) {
                newHead = lastNode;
            } else {
                prevNewNode.setNext(lastNode);
            }
            lastNode.setPrev(prevNewNode);
            prevNewNode = lastNode;
        }

        return new PersistentDoublyLinkedList<>(newHead, prevNewNode, size + 1, version);
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

        final Version version = createNewVersion();

        if (size == 1) {
            return new PersistentDoublyLinkedList<>(null, null, 0, version);
        }

        Node<E> newHead = null;
        Node<E> prevNewNode = null;

        Node<E> curNode = head;
        int pos = 0;

        while (curNode != null) {
            if (pos == index) {
                curNode = curNode.getNext();
                pos++;
                continue;
            }

            final Node<E> copied = new Node<>(curNode.getValue(), prevNewNode, null);
            if (prevNewNode == null) {
                newHead = copied;
            } else {
                prevNewNode.setNext(copied);
            }

            copied.setPrev(prevNewNode);

            prevNewNode = copied;
            curNode = curNode.getNext();
            pos++;
        }

        return new PersistentDoublyLinkedList<>(newHead, prevNewNode, size - 1, version);
    }

    /** @return node at index */
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

    /** Validates index. */
    private void checkIndex(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    /**
     * Returns iterator over list elements.
     *
     * @return iterator
     */
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
