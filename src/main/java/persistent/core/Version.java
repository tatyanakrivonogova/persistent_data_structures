package persistent.core;

/**
 * Represents a version identifier for persistent data structures.
 * Each modification to a persistent structure creates a new version,
 * allowing access to previous states of the data structure.
 * 
 * @version 1.0
 */
public interface Version {
    /**
     * Returns the unique identifier of this version.
     * 
     * @return the unique version ID as a long value
     */
    long getId();
}