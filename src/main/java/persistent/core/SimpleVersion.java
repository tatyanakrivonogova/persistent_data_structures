package persistent.core;

import java.util.UUID;

/**
 * A simple implementation of the Version interface using UUID.
 * This implementation provides version identification with strong uniqueness
 * guarantees and supports equality and hash code for use in collections.
 *
 * @version 2.0
 */
public class SimpleVersion implements Version {
    private final UUID id;

    /**
     * Constructs a new SimpleVersion with the specified UUID.
     *
     * @param id the unique version identifier as UUID
     */
    public SimpleVersion(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }

    /**
     * Constructs a new SimpleVersion with a randomly generated UUID.
     * This constructor is useful for creating new unique versions.
     */
    public SimpleVersion() {
        this(UUID.randomUUID());
    }

    /**
     * Constructs a new SimpleVersion from a UUID string representation.
     *
     * @param uuidString the string representation of UUID
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public SimpleVersion(String uuidString) {
        this(UUID.fromString(uuidString));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() { 
        return id; 
    }

    /**
     * Returns a string representation of this version in the format "Version{id}".
     *
     * @return string representation of this version
     */
    @Override
    public String toString() {
        return "Version{" + id + "}";
    }

    /**
     * Compares this version with another object for equality.
     * Two SimpleVersion objects are equal if they have the same UUID.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimpleVersion that = (SimpleVersion) obj;
        return id.equals(that.id);
    }

    /**
     * Returns a hash code value for this version based on its UUID.
     *
     * @return hash code value for this version
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
