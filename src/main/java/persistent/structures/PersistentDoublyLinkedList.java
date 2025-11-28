package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;

import java.util.Iterator;
import java.util.NoSuchElementException;


public final class PersistentDoublyLinkedList<E extends Comparable<E>> extends AbstractPersistentStructure<E> {

    private static final class Node<T> {
        final T value;
        Node<T> prev;
        Node<T> next;

        Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Node<E> head;
    private final Node<E> tail;
    private final int size;

    private PersistentDoublyLinkedList(Node<E> head, Node<E> tail, int size) {
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    private PersistentDoublyLinkedList(Node<E> head, Node<E> tail, int size, Version version) {
        super(version);
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    public static <T extends Comparable<T>> PersistentDoublyLinkedList<T> empty() {
        return new PersistentDoublyLinkedList<>(null, null, 0);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public E get(int index) {
        checkIndex(index);
        Node<E> n = nodeAt(index);
        return n.value;
    }

    public PersistentDoublyLinkedList<E> addFirst(E value) {
        return add(0, value);
    }

    public PersistentDoublyLinkedList<E> addLast(E value) {
        return add(size, value);
    }

    public PersistentDoublyLinkedList<E> add(int index, E value) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("index: " + index);

        Version version = createNewVersion();

        if (size == 0) {
            Node<E> n = new Node<>(value, null, null);
            return new PersistentDoublyLinkedList<>(n, n, 1, version);
        }

        Node<E> newHead = null;
        Node<E> prevNew = null;
        Node<E> insertedNode = null;
        int i = 0;
        Node<E> cur = head;

        while (cur != null) {
            if (i == index) {
                insertedNode = new Node<>(value, prevNew, null);
                if (prevNew == null) {
                    newHead = insertedNode;
                } else {
                    prevNew.next = insertedNode;
                }
                prevNew = insertedNode;
            }

            Node<E> copy = new Node<>(cur.value, prevNew, null);
            if (prevNew == null) {
                newHead = copy;
            } else {
                prevNew.next = copy;
            }
            prevNew = copy;

            cur = cur.next;
            i++;
        }

        if (index == size) {
            Node<E> last = new Node<>(value, prevNew, null);
            if (prevNew == null) {
                newHead = last;
            } else {
                prevNew.next = last;
            }
            prevNew = last;
        }

        Node<E> newTail = prevNew;
        return new PersistentDoublyLinkedList<>(newHead, newTail, size + 1, version);
    }

    public PersistentDoublyLinkedList<E> removeFirst() {
        if (size == 0) throw new NoSuchElementException("removeFirst from empty list");
        return remove(0);
    }

    public PersistentDoublyLinkedList<E> removeLast() {
        if (size == 0) throw new NoSuchElementException("removeLast from empty list");
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
        int i = 0;
        Node<E> cur = head;
        while (cur != null) {
            if (i == index) {
                cur = cur.next;
                i++;
                continue;
            }
            Node<E> copy = new Node<>(cur.value, prevNew, null);
            if (prevNew == null) newHead = copy;
            else prevNew.next = copy;
            prevNew = copy;

            cur = cur.next;
            i++;
        }
        Node<E> newTail = prevNew;
        return new PersistentDoublyLinkedList<>(newHead, newTail, size - 1, version);
    }

    private Node<E> nodeAt(int index) {
        Node<E> cur;
        if (index < (size >> 1)) {
            cur = head;
            for (int i = 0; i < index; i++) cur = cur.next;
        } else {
            cur = tail;
            for (int i = size - 1; i > index; i--) cur = cur.prev;
        }
        return cur;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> cur = head;
            @Override
            public boolean hasNext() { return cur != null; }
            @Override
            public E next() {
                if (cur == null) throw new NoSuchElementException();
                E v = cur.value;
                cur = cur.next;
                return v;
            }
        };
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        Node<E> cur = head;
        while (cur != null) {
            sb.append(cur.value);
            cur = cur.next;
            if (cur != null) sb.append(", ");
        }
        sb.append(']');
        return sb.toString();
    }
}
