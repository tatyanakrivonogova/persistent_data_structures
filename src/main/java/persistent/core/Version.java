package persistent.core;

import java.util.UUID;

/**
 * Represents a version identifier for persistent data structures. Each
 * modification to a persistent structure creates a new version, allowing
 * access to previous states of the data structure. Using UUID instead of
 * long provides better uniqueness guarantees, especially in distributed
 * systems.
 *
 * @version 2.0
 */
public interface Version {
  /**
   * Returns the unique identifier of this version.
   *
   * @return the unique version ID as a UUID
   */
  UUID getId();
}