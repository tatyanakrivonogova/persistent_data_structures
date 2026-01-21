package persistent.core;

/**
 * Persistent structure that maintains immutability through versioning.
 *
 * @param <T> the type of elements in this structure
 */
public interface PersistentStructure<T> {

  /**
   * Returns the current version of this persistent structure.
   *
   * @return the current version identifier
   */
  Version getVersion();

  /**
   * Returns an immutable snapshot of the current state.
   *
   * @return immutable snapshot
   */
  PersistentStructure<T> snapshot();

  /**
   * Creates a new structure with the specified element added.
   *
   * @param element the element to add
   * @return new structure with the element added
   */
  PersistentStructure<T> createWithAdded(T element);

  /**
   * Creates a new structure with the specified element removed.
   *
   * @param element the element to remove
   * @return new structure with the element removed
   */
  PersistentStructure<T> createWithRemoved(T element);

  /**
   * Creates an empty structure of the same type.
   *
   * @return empty structure
   */
  PersistentStructure<T> createEmpty();

  /**
   * Type-safe contains check.
   *
   * @param element the element to check
   * @return true if the structure contains the element
   */
  boolean containsElement(T element);
  
  /**
   * Returns the size of the structure.
   *
   * @return number of elements in the structure
   */
  int size();
  
  /**
   * Checks if the structure is empty.
   *
   * @return true if the structure is empty
   */
  boolean isEmpty();
}
