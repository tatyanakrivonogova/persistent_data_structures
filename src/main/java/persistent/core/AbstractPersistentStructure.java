package persistent.core;

import java.util.UUID;

/**
 * Abstract base class for persistent data structures that provides common
 * version management functionality. Concrete implementations should extend
 * this class to inherit basic version control capabilities.
 *
 * @param <T> the type of elements in this structure
 * @version 2.0
 */
public abstract class AbstractPersistentStructure<T>
    implements PersistentStructure<T> {
    /**
     * The current version of this persistent structure.
     */
    private Version currentVersion;

    /**
     * Constructs a new abstract persistent structure with an initial version.
     * The initial version uses a randomly generated UUID.
     */
    protected AbstractPersistentStructure() {
        this.currentVersion = new SimpleVersion(); // Uses random UUID
    }

    /**
     * Constructs a new abstract persistent structure with specified version.
     * This is used when creating new versions of existing structures.
     *
     * @param version the version to assign to this structure
     * @throws IllegalArgumentException if version is null
     */
    protected AbstractPersistentStructure(final Version version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
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
     * Each new version gets a randomly generated UUID.
     *
     * @return a new Version with a unique UUID
     */
    protected Version createNewVersion() {
        return new SimpleVersion(); // Creates new random UUID
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
