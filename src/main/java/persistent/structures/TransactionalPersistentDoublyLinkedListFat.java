package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent doubly linked lists that provides
 * mutable Collection interface. Implements the 6-step transaction process.
 *
 * @param <E> the type of elements, must be Comparable
 */
public final class TransactionalPersistentDoublyLinkedListFat<E extends
        Comparable<E>> implements Collection<E> {

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

    @Override
    public boolean add(final E e) {
        return modify(list ->
                (PersistentDoublyLinkedListFat<E>) list.createWithAdded(e)
        );
    }

    @Override
    public boolean remove(final Object o) {
        try {
            @SuppressWarnings("unchecked")
            E element = (E) o;
            return modify(list ->
                    (PersistentDoublyLinkedListFat<E>) list
                            .createWithRemoved(element)
            );
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean contains(final Object o) {
        return currentRef.get().contains(o);
    }

    @Override
    public int size() {
        return currentRef.get().size();
    }

    @Override
    public boolean isEmpty() {
        return currentRef.get().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return currentRef.get().iterator();
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

    @Override
    public boolean containsAll(final Collection<?> c) {
        return currentRef.get().containsAll(c);
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
                        result = (PersistentDoublyLinkedListFat<E>) result
                                .createWithAdded(element);
                    }
                    return result;
                });
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
        if (c.isEmpty()) {
            boolean wasEmpty = isEmpty();
            clear();
            return !wasEmpty;
        }

        return modify(
                list -> {
                    PersistentDoublyLinkedListFat<E> newList =
                            new PersistentDoublyLinkedListFat<>();
                    boolean changed = false;

                    for (E element : list) {
                        if (c.contains(element)) {
                            newList = (PersistentDoublyLinkedListFat<E>) newList
                                    .createWithAdded(element);
                        } else {
                            changed = true;
                        }
                    }

                    // Check if size changed (some elements were removed)
                    if (list.size() != newList.size()) {
                        changed = true;
                    }

                    return changed ? newList : list;
                });
    }

    @Override
    public void clear() {
        modify(list ->
                new PersistentDoublyLinkedListFat<>());
    }

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

    // List-specific methods

    /**
     * Returns the element at the specified position.
     *
     * @param index the index of the element
     * @return the element at the specified position
     */
    public E get(final int index) {
        return currentRef.get().get(index);
    }

    /**
     * Adds the specified element to the beginning of the list.
     *
     * @param e the element to add
     * @return true if the list changed as a result of the call
     */
    public boolean addFirst(final E e) {
        return modify(list -> list.addFirst(e));
    }

    /**
     * Adds the specified element to the end of the list.
     *
     * @param e the element to add
     * @return true if the list changed as a result of the call
     */
    public boolean addLast(final E e) {
        return modify(list -> list.addLast(e));
    }

    /**
     * Inserts the specified element at the specified position.
     *
     * @param index the index at which to insert
     * @param element the element to insert
     * @return true if the list changed as a result of the call
     */
    public boolean add(final int index, final E element) {
        return modify(list -> list.add(index, element));
    }

    /**
     * Removes and returns the first element of this list.
     *
     * @return the first element
     * @throws NoSuchElementException if the list is empty
     */
    public E removeFirst() {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeFirst from empty list");
        }
        E first = current.get(0);
        modify(list -> list.removeFirst());
        return first;
    }

    /**
     * Removes and returns the last element of this list.
     *
     * @return the last element
     * @throws NoSuchElementException if the list is empty
     */
    public E removeLast() {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeLast from empty list");
        }
        E last = current.get(current.size() - 1);
        modify(list -> list.removeLast());
        return last;
    }

    /**
     * Removes the element at the specified position.
     *
     * @param index the index of the element to remove
     * @return the element that was removed
     */
    public E remove(final int index) {
        PersistentDoublyLinkedListFat<E> current = currentRef.get();
        E element = current.get(index);
        modify(list -> list.remove(index));
        return element;
    }

    @Override
    public String toString() {
        return currentRef.get().toString();
    }
}
