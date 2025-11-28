package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Persistent, immutable doubly linked list.
 *
 * @param <E> element type
 */
public class PersistentDoublyLinkedList<E extends Comparable<E>>
        extends AbstractPersistentStructure<E> {

    /**
     * Doubly linked list node.
     */
    private static final class Node<T> {

        /** Node value. */
        private final T value;

        /** Previous node. */
        private Node<T> prev;

        /** Next node. */
        private Node<T> next;

        Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        public T getValue() {
            return value;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public void setPrev(Node<T> prev) {
            this.prev = prev;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }
    }

    private final Node<E> head;
    private final Node<E> tail;
    private final int size;

    private PersistentDoublyLinkedList(Node<E> head,
                                       Node<E> tail,
                                       int size,
                                       Version version
    ) {
        super(version);
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    /**
     * Empty doubly linked list constructor.
     */
    public PersistentDoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public E get(int index) {
        checkIndex(index);
        return nodeAt(index).getValue();
    }

    public PersistentDoublyLinkedList<E> addFirst(E value) {
        return add(0, value);
    }

    public PersistentDoublyLinkedList<E> addLast(E value) {
        return add(size, value);
    }

    public PersistentDoublyLinkedList<E> add(int index, E value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index: " + index);
        }

        Version version = createNewVersion();

        if (size == 0) {
            Node<E> n = new Node<>(value, null, null);
            return new PersistentDoublyLinkedList<>(n, n, 1, version);
        }

        Node<E> newHead = null;
        Node<E> prevNew = null;
        Node<E> insertedNode;

        Node<E> cur = head;
        int i = 0;

        while (cur != null) {

            if (i == index) {
                insertedNode = new Node<>(value, prevNew, null);
                if (prevNew == null) {
                    newHead = insertedNode;
                } else {
                    prevNew.setNext(insertedNode);
                }
                prevNew = insertedNode;
            }

            Node<E> copy = new Node<>(cur.getValue(), prevNew, null);
            if (prevNew == null) {
                newHead = copy;
            } else {
                prevNew.setNext(copy);
            }

            copy.setPrev(prevNew);

            prevNew = copy;
            cur = cur.getNext();
            i++;
        }

        if (index == size) {
            Node<E> last = new Node<>(value, prevNew, null);
            if (prevNew == null) {
                newHead = last;
            } else {
                prevNew.setNext(last);
            }
            last.setPrev(prevNew);
            prevNew = last;
        }

        Node<E> newTail = prevNew;
        return new PersistentDoublyLinkedList<>
                (newHead, newTail, size + 1, version);
    }

    public PersistentDoublyLinkedList<E> removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("removeFirst from empty list");
        }
        return remove(0);
    }

    public PersistentDoublyLinkedList<E> removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("removeLast from empty list");
        }
        return remove(size - 1);
    }

    public PersistentDoublyLinkedList<E> remove(int index) {
        checkIndex(index);

        Version version = createNewVersion();

        if (size == 1) {
            return new PersistentDoublyLinkedList<>(null, null, 0, version);
        }

        Node<E> newHead = null;
        Node<E> prevNew = null;

        Node<E> cur = head;
        int i = 0;

        while (cur != null) {
            if (i == index) {
                cur = cur.getNext();
                i++;
                continue;
            }

            Node<E> copy = new Node<>(cur.getValue(), prevNew, null);
            if (prevNew == null) {
                newHead = copy;
            } else {
                prevNew.setNext(copy);
            }
            copy.setPrev(prevNew);

            prevNew = copy;
            cur = cur.getNext();
            i++;
        }

        Node<E> newTail = prevNew;
        return new PersistentDoublyLinkedList<>
                (newHead, newTail, size - 1, version);
    }

    private Node<E> nodeAt(int index) {
        Node<E> cur;

        if (index < (size >> 1)) {
            cur = head;
            for (int i = 0; i < index; i++) {
                assert cur != null;
                cur = cur.getNext();
            }
        } else {
            cur = tail;
            for (int i = size - 1; i > index; i--) {
                assert cur != null;
                cur = cur.getPrev();
            }
        }

        return cur;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
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
                E value = cur.getValue();
                cur = cur.getNext();
                return value;
            }
        };
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        Node<E> cur = head;
        while (cur != null) {
            sb.append(cur.getValue());
            cur = cur.getNext();
            if (cur != null) sb.append(", ");
        }

        sb.append(']');
        return sb.toString();
    }
}
