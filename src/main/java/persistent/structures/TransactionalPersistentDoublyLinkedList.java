package persistent.structures;

import persistent.core.PersistentStructure;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent doubly linked lists that provides mutable Collection interface.
 * Implements the 6-step transaction process.
 */
public class TransactionalPersistentDoublyLinkedList<E extends Comparable<E>> 
        implements Collection<E> {
    
    private final AtomicReference<PersistentDoublyLinkedList<E>> currentRef;
    
    public TransactionalPersistentDoublyLinkedList() {
        this.currentRef = new AtomicReference<>(new PersistentDoublyLinkedList<>());
    }
    
    public TransactionalPersistentDoublyLinkedList(PersistentDoublyLinkedList<E> initial) {
        this.currentRef = new AtomicReference<>(initial);
    }
    
    /**
     * Executes a modification operation atomically.
     */
    private boolean modify(java.util.function.Function<PersistentDoublyLinkedList<E>, 
                           PersistentDoublyLinkedList<E>> operation) {
        while (true) {
            PersistentDoublyLinkedList<E> current = currentRef.get();
            PersistentDoublyLinkedList<E> newVersion = operation.apply(current);
            
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
    public boolean add(E e) {
        return modify(list -> (PersistentDoublyLinkedList<E>) list.createWithAdded(e));
    }
    
    @Override
    public boolean remove(Object o) {
        try {
            @SuppressWarnings("unchecked")
            E element = (E) o;
            return modify(list -> (PersistentDoublyLinkedList<E>) list.createWithRemoved(element));
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public boolean contains(Object o) {
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
    public <T> T[] toArray(T[] a) {
        return currentRef.get().toArray(a);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return currentRef.get().containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        return modify(list -> {
            PersistentDoublyLinkedList<E> result = list;
            for (E element : c) {
                result = (PersistentDoublyLinkedList<E>) result.createWithAdded(element);
            }
            return result;
        });
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }
        return modify(list -> {
            PersistentDoublyLinkedList<E> result = list;
            boolean changed = false;
            for (Object obj : c) {
                try {
                    @SuppressWarnings("unchecked")
                    E element = (E) obj;
                    PersistentDoublyLinkedList<E> newResult = (PersistentDoublyLinkedList<E>) result.createWithRemoved(element);
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
    public boolean retainAll(Collection<?> c) {
        if (c.isEmpty()) {
            boolean wasEmpty = isEmpty();
            clear();
            return !wasEmpty;
        }
        
        return modify(list -> {
            PersistentDoublyLinkedList<E> newList = new PersistentDoublyLinkedList<>();
            boolean changed = false;
            
            for (E element : list) {
                if (c.contains(element)) {
                    newList = (PersistentDoublyLinkedList<E>) newList.createWithAdded(element);
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
        modify(list -> new PersistentDoublyLinkedList<>());
    }
    
    /**
     * Returns the current immutable snapshot.
     */
    public PersistentDoublyLinkedList<E> snapshot() {
        return currentRef.get();
    }
    
    /**
     * Returns a transactional wrapper for the current snapshot.
     */
    public TransactionalPersistentDoublyLinkedList<E> transactionalCopy() {
        return new TransactionalPersistentDoublyLinkedList<>(currentRef.get());
    }
    
    // List-specific methods
    
    public E get(int index) {
        return currentRef.get().get(index);
    }
    
    public boolean addFirst(E e) {
        return modify(list -> list.addFirst(e));
    }
    
    public boolean addLast(E e) {
        return modify(list -> list.addLast(e));
    }
    
    public boolean add(int index, E element) {
        return modify(list -> list.add(index, element));
    }
    
    public E removeFirst() {
        PersistentDoublyLinkedList<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeFirst from empty list");
        }
        E first = current.get(0);
        modify(list -> list.removeFirst());
        return first;
    }
    
    public E removeLast() {
        PersistentDoublyLinkedList<E> current = currentRef.get();
        if (current.isEmpty()) {
            throw new NoSuchElementException("removeLast from empty list");
        }
        E last = current.get(current.size() - 1);
        modify(list -> list.removeLast());
        return last;
    }
    
    public E remove(int index) {
        PersistentDoublyLinkedList<E> current = currentRef.get();
        E element = current.get(index);
        modify(list -> list.remove(index));
        return element;
    }
    
    @Override
    public String toString() {
        return currentRef.get().toString();
    }
}