package persistent.structures;

import java.lang.annotation.Inherited;
import java.util.*;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Fat-node persistent doubly linked list.
 * Only the newest version can be modified (fat nodes).
 * Old versions remain immutable.
 *
 * @param <E> element type
 */
public final class PersistentDoublyLinkedListFat<E extends Comparable<E>>
        implements PersistentStructure<E> {

    /**
     * Node with fat fields
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

        Node(final T value, final Node<T> prev, final Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }
    }

    /** First element. */
    private final Node<E> head;

    /** Last element. */
    private final Node<E> tail;

    /** Collection size. */
    private final int size;

    /** Private constructor */
    private PersistentDoublyLinkedListFat(final Node<E> head, final Node<E> tail, final int size) {
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    /** Create empty list */
    public PersistentDoublyLinkedListFat() {
        this(null, null, 0);
    }

    /** Create read-only snapshot version */
    private PersistentDoublyLinkedListFat<E> snapshotVersion() {
        return new PersistentDoublyLinkedListFat<>(head, tail, size);
    }

    /** Copy full list into new mutable version */
    private PersistentDoublyLinkedListFat<E> fork() {
        Node<E> old = head;
        Node<E> newHead = null;
        Node<E> newPrev = null;

        while (old != null) {
            Node<E> n = new Node<>(old.value, newPrev, null);
            if (newPrev != null) newPrev.next = n;
            else newHead = n;

            newPrev = n;
            old = old.next;
        }

        return new PersistentDoublyLinkedListFat<>(newHead, newPrev, size);
    }


    // ========== PersistentStructure ==========

    @Override
    public PersistentStructure<E> createWithAdded(final E element) {
        return add(size, element);
    }

    @Override
    public PersistentStructure<E> createWithRemoved(final E element) {
        Node<E> cur = head;
        int index = 0;
        while (cur != null) {
            if (Objects.equals(cur.value, element)) return remove(index);
            cur = cur.next;
            index++;
        }
        return this;
    }

    /**
    * @inheritDoc
    */
    @Override
    public PersistentStructure<E> createEmpty() {
        return new PersistentDoublyLinkedListFat<>();
    }

    /**
     * Check if element contains in the list.
     */
    @Override
    public boolean containsElement(final E element) {
        Node<E> n = head;
        while (n != null) {
            if (Objects.equals(n.value, element)) return true;
            n = n.next;
        }
        return false;
    }

    @Override
    public Version getVersion() {
        return new SimpleVersion();
    }

    @Override
    public PersistentStructure<E> snapshot() {
        return snapshotVersion();
    }

    // ========== Collection stubs ==========

    @Override
    public boolean add(final E e) { throw new UnsupportedOperationException(); }

    @Override
    public boolean remove(final Object o) { throw new UnsupportedOperationException(); }

    @Override
    public boolean contains(final Object o) {
        try { return containsElement((E)o); }
        catch (ClassCastException ex) { return false; }
    }

    @Override public int size() { return size; }

    @Override public boolean isEmpty() { return size == 0; }

    @Override public Iterator<E> iterator() {
        return new Iterator<>() {
            Node<E> cur = head;
            public boolean hasNext() { return cur != null; }
            public E next() {
                if (cur == null) throw new NoSuchElementException();
                E val = cur.value;
                cur = cur.next;
                return val;
            }
        };
    }

    @Override public Object[] toArray() {
        Object[] a = new Object[size];
        int i=0; for (E e:this) a[i++] = e;
        return a;
    }

    @Override public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i=0;
        for (E e:this) a[i++] = (T)e;
        if (a.length > size) a[size] = null;
        return a;
    }

    @Override public boolean containsAll(final Collection<?> c) {
        for (Object o:c) if (!contains(o)) return false;
        return true;
    }

    @Override public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
    @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }

    // ========== List operations ==========

    /**
     * Returns element by index.
     *
     * @param index index of element
     * @return element value
     */
    public E get(final int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    /**
     * Adds value to the beginning of the list.
     *
     * @param v new value
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> addFirst(final E v) { return add(0, v); }

    /**
     * Adds value to the end of the list.
     *
     * @param v new value
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> addLast(final E v)  { return add(size, v); }

    /**
     * Removes the first element.
     *
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> removeFirst() { return remove(0); }

    /**
     * Removes the last element.
     *
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> removeLast()  { return remove(size-1); }

    /**
     * Inserts a value at a given index.
     *
     * @param index target index
     * @param value element to add
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> add(final int index, final E value) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        PersistentDoublyLinkedListFat<E> self = fork();

        if (self.size == 0) {
            Node<E> n = new Node<>(value, null, null);
            return new PersistentDoublyLinkedListFat<>(n, n, 1);
        }

        if (index == self.size) {
            Node<E> n = new Node<>(value, self.tail, null);
            self.tail.next = n;
            return new PersistentDoublyLinkedListFat<>(self.head, n, size+1);
        }

        if (index == 0) {
            Node<E> n = new Node<>(value, null, self.head);
            self.head.prev = n;
            return new PersistentDoublyLinkedListFat<>(n, self.tail, size+1);
        }

        Node<E> cur = self.nodeAt(index);
        Node<E> n = new Node<>(value, cur.prev, cur);
        cur.prev.next = n;
        cur.prev = n;
        return new PersistentDoublyLinkedListFat<>(self.head, self.tail, size+1);
    }

    /**
     * Removes element at index.
     *
     * @param index index of removable element
     * @return new list version
     */
    public PersistentDoublyLinkedListFat<E> remove(final int index) {
        checkIndex(index);
        PersistentDoublyLinkedListFat<E> self = fork();

        Node<E> cur = self.nodeAt(index);

        Node<E> newHead = self.head;
        Node<E> newTail = self.tail;

        if (cur == self.head) newHead = cur.next;
        if (cur == self.tail) newTail = cur.prev;

        if (cur.prev != null) cur.prev.next = cur.next;
        if (cur.next != null) cur.next.prev = cur.prev;

        return new PersistentDoublyLinkedListFat<>(newHead, newTail, size-1);
    }

    /**
     * Get node at index.
     *
     * @param index node index
     * @return node at index
     */
    private Node<E> nodeAt(final int index) {
        Node<E> c;
        if (index < (size>>1)) {
            c = head;
            for (int i=0;i<index;i++) c = c.next;
        } else {
            c = tail;
            for (int i=size-1;i>index;i--) c = c.prev;
        }
        return c;
    }

    /**
     * Validates index.
     *
     * @param index index to validate
     */
    private void checkIndex(final int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Collection<?> that)) {
            return false;
        }

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

    /**
     * Returns string representation.
     *
     * @return string representation
     */
    @Override public String toString() {
        StringBuilder sb=new StringBuilder("[");
        Node<E> c=head;
        while(c!=null){
            sb.append(c.value);
            c=c.next;
            if(c!=null) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
