package persistent.core;

/**
 * A simple implementation of the Version interface using sequential numbering.
 * This implementation provides basic version identification with equality
 * and hash code support for use in collections.
 * 
 * @version 1.0
 */
public class SimpleVersion implements Version {
    private final long id;
    
    /**
     * Constructs a new SimpleVersion with the specified identifier.
     * 
     * @param id the unique version identifier
     */
    public SimpleVersion(long idd) {
        this.id = id;
    }
    
    /**s
     * {@inheritDoc}
     */
    @Override
    public long getId() { 
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
     * Two SimpleVersion objects are equal if they have the same ID.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimpleVersion that = (SimpleVersion) obj;
        return id == that.id;
    }
    
    /**
     * Returns a hash code value for this version based on its ID.
     * 
     * @return hash code value for this version
     */
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}