package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class PersistentDoublyLinkedListOptimized<E extends Comparable<E>>
        extends AbstractPersistentStructure<E> {

    private static final class Node<T> {
        final T value;
        Node<T> prev;
        Node<T> next;

        Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        Node<T> copy() {
            return new Node<>(value, null, null);
        }
    }

    private final Node<E> head;
    private final Node<E> tail;
    private final int size;

    public PersistentDoublyLinkedListOptimized() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    private PersistentDoublyLinkedListOptimized(Node<E> head,
                                                Node<E> tail,
                                                int size,
                                                Version v) {
        super(v);
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    public int size() { return size; }

    public E get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    public PersistentDoublyLinkedListOptimized<E> addFirst(E v) {
        return add(0, v);
    }

    public PersistentDoublyLinkedListOptimized<E> addLast(E v) {
        return add(size, v);
    }

    public PersistentDoublyLinkedListOptimized<E> add(int index, E value) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();

        Version ver = createNewVersion();

        // empty
        if (size == 0) {
            Node<E> n = new Node<>(value, null, null);
            return new PersistentDoublyLinkedListOptimized<>(n, n, 1, ver);
        }

        // insert at head
        if (index == 0) {
            Node<E> inserted = new Node<>(value, null, null);
            Node<E> newHead = inserted;

            // copy forward full list
            Node<E> newTail = copyForwardFrom(inserted, head);
            return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size + 1, ver);
        }

        // insert at tail
        if (index == size) {
            // copy backward from tail
            Node<E> newTail = new Node<>(value, null, null);
            Node<E> prevCopy = tail.copy();
            prevCopy.next = newTail;
            newTail.prev = prevCopy;

            Node<E> newHead = copyBackwardFrom(prevCopy, tail.prev);
            return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size + 1, ver);
        }

        // insert middle
        Node<E> target = nodeAt(index);
        Node<E> before = target.prev;

        Node<E> newTarget = target.copy();
        Node<E> newBefore = before.copy();
        Node<E> inserted = new Node<>(value, null, null);

        newBefore.next = inserted;
        inserted.prev = newBefore;

        inserted.next = newTarget;
        newTarget.prev = inserted;

        Node<E> newHead = copyBackwardFrom(newBefore, before.prev);
        Node<E> newTail = copyForwardFrom(newTarget, target.next);

        return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size + 1, ver);
    }

    public PersistentDoublyLinkedListOptimized<E> removeFirst() {
        return remove(0);
    }

    public PersistentDoublyLinkedListOptimized<E> removeLast() {
        return remove(size - 1);
    }

    public PersistentDoublyLinkedListOptimized<E> remove(int index) {
        checkIndex(index);
        Version ver = createNewVersion();

        if (size == 1)
            return new PersistentDoublyLinkedListOptimized<>(null, null, 0, ver);

        // remove head
        if (index == 0) {
            Node<E> newHead = head.next.copy();
            Node<E> newTail = copyForwardFrom(newHead, head.next.next);
            return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size - 1, ver);
        }

        // remove tail
        if (index == size - 1) {
            Node<E> prev = tail.prev;
            Node<E> newTail = prev.copy();
            Node<E> newHead = copyBackwardFrom(newTail, prev.prev);
            return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size - 1, ver);
        }

        // remove middle
        Node<E> target = nodeAt(index);
        Node<E> p = target.prev;
        Node<E> n = target.next;

        Node<E> newP = p.copy();
        Node<E> newN = n.copy();

        newP.next = newN;
        newN.prev = newP;

        Node<E> newHead = copyBackwardFrom(newP, p.prev);
        Node<E> newTail = copyForwardFrom(newN, n.next);

        return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size - 1, ver);
    }


    // ----------- Core Copy Logic -----------

    private Node<E> copyBackwardFrom(Node<E> startCopy, Node<E> origPrev) {
        Node<E> curCopy = startCopy;
        Node<E> curOld = origPrev;

        while (curOld != null) {
            Node<E> copy = curOld.copy();
            copy.next = curCopy;
            curCopy.prev = copy;
            curCopy = copy;
            curOld = curOld.prev;
        }
        return curCopy; // new head
    }

    private Node<E> copyForwardFrom(Node<E> startCopy, Node<E> origNext) {
        Node<E> curCopy = startCopy;
        Node<E> curOld = origNext;

        while (curOld != null) {
            Node<E> copy = curOld.copy();
            curCopy.next = copy;
            copy.prev = curCopy;
            curCopy = copy;
            curOld = curOld.next;
        }
        return curCopy; // new tail
    }


    // ----------- Helpers -----------

    private Node<E> nodeAt(int index) {
        Node<E> x;
        if (index < (size >> 1)) {
            x = head;
            for (int i = 0; i < index; i++) x = x.next;
        } else {
            x = tail;
            for (int i = size - 1; i > index; i--) x = x.prev;
        }
        return x;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            Node<E> cur = head;

            @Override
            public boolean hasNext() { return cur != null; }

            @Override
            public E next() {
                if (cur == null) throw new NoSuchElementException();
                E val = cur.value;
                cur = cur.next;
                return val;
            }
        };
    }
}
