package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Persistent doubly linked list with true path copying.
 */
public final class PersistentDoublyLinkedListOptimized<E extends Comparable<E>>
        extends AbstractPersistentStructure<E> {

    /** Node for path-copying persistent list. */
    private static final class Node<T> {
        final T value;
        Node<T> prev;
        Node<T> next;

        Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        Node<T> copyClean() {
            return new Node<>(value, null, null);
        }
    }

    private final Node<E> head;
    private final Node<E> tail;
    private final int size;

    private PersistentDoublyLinkedListOptimized(Node<E> head,
                                                Node<E> tail,
                                                int size,
                                                Version v) {
        super(v);
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    public PersistentDoublyLinkedListOptimized() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public boolean isEmpty() { return size == 0; }
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

        Version version = createNewVersion();

        // empty list
        if (size == 0) {
            Node<E> n = new Node<>(value, null, null);
            return new PersistentDoublyLinkedListOptimized<>(n, n, 1, version);
        }

        // insert at head
        if (index == 0) {
            Node<E> oldHead = head;
            Node<E> newHead = oldHead.copyClean();
            newHead.next = oldHead.next; // temp
            newHead.prev = null;

            Node<E> inserted = new Node<>(value, null, newHead);
            newHead.prev = inserted;

            // now path-copy forward from newHead to tail
            Node<E> finalHead = inserted;
            Node<E> finalTail = copyForward(newHead);

            return new PersistentDoublyLinkedListOptimized<>(finalHead, finalTail, size + 1, version);
        }

        // insert at tail
        if (index == size) {
            Node<E> oldTail = tail;
            Node<E> newTail = oldTail.copyClean();
            Node<E> inserted = new Node<>(value, newTail, null);
            newTail.next = inserted;
            return rebuildBackward(newTail, version, size + 1);
        }

        // insert in middle
        Node<E> cur = nodeAt(index);
        Node<E> prev = cur.prev;

        Node<E> newCur = cur.copyClean();
        Node<E> newPrev = prev.copyClean();
        Node<E> inserted = new Node<>(value, newPrev, newCur);

        newPrev.next = inserted;
        newCur.prev = inserted;

        return rebuildBackward(newPrev, version, size + 1);
    }

    public PersistentDoublyLinkedListOptimized<E> removeFirst() {
        if (size == 0) throw new NoSuchElementException();
        return remove(0);
    }

    public PersistentDoublyLinkedListOptimized<E> removeLast() {
        if (size == 0) throw new NoSuchElementException();
        return remove(size - 1);
    }

    public PersistentDoublyLinkedListOptimized<E> remove(int index) {
        checkIndex(index);
        Version v = createNewVersion();

        if (size == 1) {
            return new PersistentDoublyLinkedListOptimized<>(null, null, 0, v);
        }

        // removing head
        if (index == 0) {
            Node<E> oldNext = head.next;
            Node<E> newHead = oldNext.copyClean();
            Node<E> newTail = copyForward(newHead);
            return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, size - 1, v);
        }

        // removing tail
        if (index == size - 1) {
            Node<E> oldPrev = tail.prev;
            Node<E> newTail = oldPrev.copyClean();
            return rebuildBackward(newTail, v, size - 1);
        }

        // removing in middle
        Node<E> target = nodeAt(index);
        Node<E> p = target.prev;
        Node<E> n = target.next;

        Node<E> newP = p.copyClean();
        Node<E> newN = n.copyClean();

        newP.next = newN;
        newN.prev = newP;

        return rebuildBackward(newP, v, size - 1);
    }

    /**
     * Copies path backward до головы.
     */
    private PersistentDoublyLinkedListOptimized<E> rebuildBackward(Node<E> start,
                                                                   Version v,
                                                                   int newSize) {

        Node<E> curNew = start;
        Node<E> curOld = start.prev; // original previous node

        // path-copy backward
        while (curOld != null) {
            Node<E> copy = curOld.copyClean();

            copy.next = curNew;
            curNew.prev = copy;

            curNew = copy;
            curOld = curOld.prev;
        }

        Node<E> newHead = curNew;

        // then forward to restore tail
        Node<E> newTail = copyForward(newHead);

        return new PersistentDoublyLinkedListOptimized<>(newHead, newTail, newSize, v);
    }

    /**
     * Copies path forward (next direction) until end.
     * Used after reconstructing head.
     */
    private Node<E> copyForward(Node<E> newHead) {
        Node<E> curNew = newHead;
        Node<E> curOld = newHead.next;

        while (curOld != null) {
            Node<E> copy = curOld.copyClean();
            curNew.next = copy;
            copy.prev = curNew;

            curNew = copy;
            curOld = curOld.next;
        }
        return curNew; // final tail
    }

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

    @Override
    public String toString() {
        if (size == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Node<E> x = head;
        while (x != null) {
            sb.append(x.value);
            x = x.next;
            if (x != null) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
