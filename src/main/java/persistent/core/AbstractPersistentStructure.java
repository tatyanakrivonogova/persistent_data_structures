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
public abstract class AbstractPersistentStructure<T> implements PersistentStructure<T> {
    /**
     * The current version of this persistent structure.
     */
    protected Version currentVersion;

    /**
     * Constructs a new abstract persistent structure with an initial version.
     * The initial version uses a randomly generated UUID.
     */
    protected AbstractPersistentStructure() {
        this.currentVersion = new SimpleVersion(); // Uses random UUID
    }

    /**
     * Constructs a new abstract persistent structure with the specified version.
     * This constructor is used when creating new versions of existing structures.
     *
     * @param version the version to assign to this structure
     * @throws IllegalArgumentException if version is null
     */
    protected AbstractPersistentStructure(Version version) {
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
     * Creates a new version based on the current version with a custom UUID.
     * This method is useful when you need to create a version with a specific UUID.
     *
     * @param uuid the UUID for the new version
     * @return a new Version with the specified UUID
     */
    protected Version createNewVersion(UUID uuid) {
        return new SimpleVersion(uuid);
    }

    /**
     * Creates a new version based on the current version from a UUID string.
     *
     * @param uuidString the string representation of the UUID
     * @return a new Version with the specified UUID
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    protected Version createNewVersion(String uuidString) {
        return new SimpleVersion(uuidString);
    }

    /**
     * Updates the current version of this structure.
     * This method should be called when the structure is modified.
     *
     * @return the new version that was set
     */
    protected Version updateVersion() {
        this.currentVersion = createNewVersion();
        return this.currentVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Creates a deep copy of the current version.
     * Useful for creating new structure instances that reference the same version.
     *
     * @return a copy of the current version
     */
    protected Version copyCurrentVersion() {
        UUID currentId = currentVersion.getId();
        return new SimpleVersion(currentId);
    }
}
