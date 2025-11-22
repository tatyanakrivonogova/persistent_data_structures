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
     * Returns an iterator over the elements in this structure.
     * 
     * @return an iterator over the elements in this structure
     */
    Iterator<T> iterator();
    
    /**
     * Returns a string representation of this structure.
     * 
     * @return string representation of this structure
     */
    String toString();
}