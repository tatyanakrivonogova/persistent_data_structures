package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent doubly linked lists that provides
 * mutable List interface.
 *
 * @param <E> the type of elements, must be Comparable
 */
@SuppressWarnings({"LineLength", "LongLine", "MaxLineLength"})
public final class TransactionalPersistentDoublyLinkedListFat<E extends
        Comparable<E>> implements List<E> {

    /** Atomic reference to the current version of the list. */
    private final AtomicReference<PersistentDoublyLinkedListFat<E>>
            currentRef;

    /** Creates a new empty transactional list. */
    public TransactionalPersistentDoublyLinkedListFat() {
        this.currentRef =
                new AtomicReference<>(new PersistentDoublyLinkedListFat<>());
    }

    /**
     * Creates a transactional list with the given initial state.
     *
     * @param initial the initial list state
     */
    public TransactionalPersistentDoublyLinkedListFat(
            final PersistentDoublyLinkedListFat<E> initial) {
        this.currentRef = new AtomicReference<>(initial);
    }

    /**
     * Executes a modification operation atomically.
     *
     * @param operation the function to apply to the list
     * @return true if the operation succeeded and changed the list
     */
    private boolean modify(
            final java.util.function.Function<PersistentDoublyLinkedListFat<E>,
                    PersistentDoublyLinkedListFat<E>> operation) {
        while (true) {
            PersistentDoublyLinkedListFat<E> current =
                    currentRef.get();
            PersistentDoublyLinkedListFat<E> newVersion =
                    operation.apply(current);

            if (newVersion == current) {
                return false; // No changes
            }

            if (currentRef.compareAndSet(current, newVersion)) {
                return true; // Success
            }
            // Retry on conflict
        }
    }

    // ========== List interface methods ==========

    @Override
    public boolean add(final E e) {
        return modify(list -> list.addLastInternal(e));
    }

    @Override
    public void add(final int index, final E element) {
        modify(list -> list.addInternal(index, element));
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        return modify(
                list -> {
                    PersistentDoublyLinkedListFat<E> result = list;
                    for (E element : c) {
                        result = result.addLastInternal(element);
                    }
                    return result;
                });
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        return modify(
                list -> {
                    PersistentDoublyLinkedListFat<E> result = list;
                    int i = index;
                    for (E element : c) {
                        result = result.addInternal(i, element);
                        i++;
                    }
                    return result;
                });
    }

    @Override
    public void clear() {
        modify(list -> new PersistentDoublyLinkedListFat<>());
    }

    @Override
    public boolean contains(final Object o) {
        return currentRef.get().contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return currentRef.get().containsAll(c);
    }

    @Override
    public E get(final int index) {
        return currentRef.get().get(index);
    }

    @Override
    public int indexOf(final Object o) {
        return currentRef.get().indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return currentRef.get().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<E> snapshotIterator = currentRef.get().iterator();

            @Override
            public boolean hasNext() {
                return snapshotIterator.hasNext();
            }

            @Override
            public E next() {
                return snapshotIterator.next();
            }
        };
    }

    @Override
    public int lastIndexOf(final Object o) {
        return currentRef.get().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new TransactionalListIterator(index);
    }

    @Override
    public E remove(final int index) {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        E element = current.get(index);
        modify(list -> list.removeInternal(index));
        return element;
    }

    @Override
    public boolean remove(final Object o) {
        try {
            @SuppressWarnings("unchecked")
            E element = (E) o;
            return modify(list ->
                (PersistentDoublyLinkedListFat<E>) list.createWithRemoved(element));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }
        return modify(
                list -> {
                    PersistentDoublyLinkedListFat<E> result = list;
                    boolean changed = false;
                    for (Object obj : c) {
                        try {
                            @SuppressWarnings("unchecked")
                            E element = (E) obj;
                            PersistentDoublyLinkedListFat<E> newResult =
                                    (PersistentDoublyLinkedListFat<E>) result
                                            .createWithRemoved(element);
                            if (newResult != result) {
                                changed = true;
                                result = newResult;
                            }
                        } catch (ClassCastException e) {
                            // Skip elements of wrong type
                        }
                    }
                    return changed ? result : list;
                });
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return modify(
                list -> {
                    PersistentDoublyLinkedListFat<E> newList =
                            new PersistentDoublyLinkedListFat<>();
                    boolean changed = false;

                    for (E element : list) {
                        if (c.contains(element)) {
                            newList = newList.addLastInternal(element);
                        } else {
                            changed = true;
                        }
                    }

                    return changed ? newList : list;
                });
    }

    @Override
    public E set(final int index, final E element) {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        E oldElement = current.get(index);
        modify(list -> {
            PersistentDoublyLinkedListFat<E> removed = list.removeInternal(index);
            return removed.addInternal(index, element);
        });
        return oldElement;
    }

    @Override
    public int size() {
        return currentRef.get().size();
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        // Return a snapshot sublist (immutable)
        return currentRef.get().subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return currentRef.get().toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
        return currentRef.get().toArray(a);
    }

    /**
     * Adds an element to the beginning of the list.
     *
     * @param e the element to add
     */
    public void addFirst(final E e) {
        modify(list -> list.addFirstInternal(e));
    }

    /**
     * Adds an element to the end of the list.
     *
     * @param e the element to add
     */
    public void addLast(final E e) {
        modify(list -> list.addLastInternal(e));
    }

    /**
     * Removes and returns the first element of the list.
     *
     * @return the removed first element
     * @throws NoSuchElementException if the list is empty
     */
    public E removeFirst() {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeFirst from empty list");
        }
        E first = current.get(0);
        modify(list -> list.removeFirstInternal());
        return first;
    }

    /**
     * Removes and returns the last element of the list.
     *
     * @return the removed last element
     * @throws NoSuchElementException if the list is empty
     */
    public E removeLast() {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeLast from empty list");
        }
        E last = current.get(current.size() - 1);
        modify(list -> list.removeLastInternal());
        return last;
    }

    // ========== Transactional-specific methods ==========

    /**
     * Returns the current immutable snapshot.
     *
     * @return the current list snapshot
     */
    public PersistentDoublyLinkedListFat<E> snapshot() {
        return currentRef.get();
    }

    /**
     * Returns a transactional wrapper for the current snapshot.
     *
     * @return a new transactional wrapper for the current list
     */
    public TransactionalPersistentDoublyLinkedListFat<E> transactionalCopy() {
        return new TransactionalPersistentDoublyLinkedListFat<>(
                currentRef.get()
        );
    }

    @Override
    public String toString() {
        return currentRef.get().toString();
    }

    // ========== Inner classes ==========

    /**
     * Transactional list iterator that supports modifications.
     */
    private class TransactionalListIterator implements ListIterator<E> {

        /** Current cursor position. */
        private int cursor;
        
        /** Index of last returned element, or -1 if none. */
        private int lastRet = -1;

        /**
         * Creates a new list iterator starting at the specified index.
         *
         * @param index starting index
         * @throws IndexOutOfBoundsException if index is out of range
         */
        TransactionalListIterator(final int index) {
            if (index < 0 || index > size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            this.cursor = index;
        }

        @Override
        public boolean hasNext() {
            return cursor < size();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E result = get(cursor);
            lastRet = cursor;
            cursor++;
            return result;
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            cursor--;
            lastRet = cursor;
            return get(cursor);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            TransactionalPersistentDoublyLinkedListFat.this.remove(lastRet);
            if (lastRet < cursor) {
                cursor--;
            }
            lastRet = -1;
        }

        @Override
        public void set(final E e) {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            TransactionalPersistentDoublyLinkedListFat.this.set(lastRet, e);
        }

        @Override
        public void add(final E e) {
            TransactionalPersistentDoublyLinkedListFat.this.add(cursor, e);
            cursor++;
            lastRet = -1;
        }
    }
}
