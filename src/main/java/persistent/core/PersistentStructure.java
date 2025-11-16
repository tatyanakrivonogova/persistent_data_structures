package persistent.core;

import java.util.Iterator;

/**
 * The core interface for all persistent data structures.
 * Persistent structures are immutable - each modification operation returns
 * a new version of the structure while preserving the original.
 * 
 * @param <T> the type of elements in this structure
 * @version 1.0
 */
public interface PersistentStructure<T> extends Iterable<T> {
    /**
     * Updates the element at the specified position and returns a new structure.
     * 
     * @param index the index of the element to update
     * @param value the new value
     * @return a new persistent structure with the updated element
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    PersistentStructure<T> update(int index, T value);
    
    /**
     * Inserts the specified element at the specified position and returns a new structure.
     * 
     * @param index the index at which to insert the element
     * @param value the element to insert
     * @return a new persistent structure with the inserted element
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    PersistentStructure<T> insert(int index, T value);
    
    /**
     * Deletes the element at the specified position and returns a new structure.
     * 
     * @param index the index of the element to delete
     * @return a new persistent structure without the deleted element
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    PersistentStructure<T> delete(int index);
    
    /**
     * Returns the element at the specified position in this structure.
     * 
     * @param index the index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    T get(int index);
    
    /**
     * Returns the number of elements in this structure.
     * 
     * @return the number of elements in this structure
     */
    int size();
    
    /**
     * Returns true if this structure contains no elements.
     * 
     * @return true if this structure contains no elements
     */
    boolean isEmpty();
    
    /**
     * Returns the current version of this persistent structure.
     * 
     * @return the current version identifier
     */
    Version getVersion();
    
    /**
     * Returns an iterator over the elements in this structure in proper sequence.
     * 
     * @return an iterator over the elements in this structure
     */
    Iterator<T> iterator();
}