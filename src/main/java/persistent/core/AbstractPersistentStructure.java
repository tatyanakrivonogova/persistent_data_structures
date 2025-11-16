package persistent.core;

/**
 * Abstract base class for persistent data structures that provides common
 * version management functionality. Concrete implementations should extend
 * this class to inherit basic version control capabilities.
 * 
 * @param <T> the type of elements in this structure
 * @version 1.0
 */
public abstract class AbstractPersistentStructure<T> implements PersistentStructure<T> {
    /**
     * The current version of this persistent structure.
     */
    protected Version currentVersion;
    
    /**
     * Global counter for generating unique version identifiers across all structures.
     */
    protected static long versionCounter = 0;
    
    /**
     * Constructs a new abstract persistent structure with an initial version.
     */
    protected AbstractPersistentStructure() {
        this.currentVersion = new SimpleVersion(versionCounter++);
    }
    
    /**
     * Constructs a new abstract persistent structure with the specified version.
     * This constructor is used when creating new versions of existing structures.
     * 
     * @param version the version to assign to this structure
     */
    protected AbstractPersistentStructure(Version version) {
        this.currentVersion = version;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Version getVersion() {
        return currentVersion;
    }
    
    /**
     * Creates a new version identifier for use when modifying the structure.
     * 
     * @return a new Version with a unique identifier
     */
    protected Version createNewVersion() {
        return new SimpleVersion(versionCounter++);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}