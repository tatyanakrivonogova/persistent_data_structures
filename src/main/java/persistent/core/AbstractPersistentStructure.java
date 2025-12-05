package persistent.core;

import java.util.UUID;

/**
 * Abstract base class for persistent data structures with transaction support.
 *
 * @param <T> the type of elements in this structure
 * @version 3.0
 */
public abstract class AbstractPersistentStructure<T>
    implements TransactionalStructure<T> {

    /**
     * The current version of this persistent structure.
     */
    private Version currentVersion;

    /**
     * Transaction manager for this structure.
     */
    private final TransactionManager transactionManager;

    /**
     * State before transaction started (for rollback).
     */
    private PersistentStructure<T> preTransactionState;

    /**
     * Constructs a new abstract persistent structure with an initial version.
     */
    protected AbstractPersistentStructure() {
        this.currentVersion = new TransactionalVersion();
        this.transactionManager = new TransactionManager();
        this.preTransactionState = null;
    }

    /**
     * Constructs a new abstract persistent structure with specified version.
     *
     * @param version version of structure
     */
    protected AbstractPersistentStructure(final Version version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
        this.currentVersion = version;
        this.transactionManager = new TransactionManager();
        this.preTransactionState = null;
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
     * If in transaction, creates a transactional version.
     *
     * @return a new Version
     */
    protected Version createNewVersion() {
        if (transactionManager.isInTransaction()) {
            UUID transactionId = transactionManager.getCurrentTransactionId();
            int sequence = transactionManager.getNextTransactionSequence();
            return new TransactionalVersion(transactionId, sequence);
        } else {
            return new TransactionalVersion();
        }
    }

    /**
     * Sets the current version.
     * Used internally after operations.
     *
     * @param version version of structure
     */
    protected void setVersion(final Version version) {
        this.currentVersion = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginTransaction() {
        if (!transactionManager.isInTransaction()) {
            // Save current state for potential rollback
            savePreTransactionState();
        }
        transactionManager.beginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitTransaction() {
        if (transactionManager.isInTransaction()) {
            transactionManager.commitTransaction();

            // If this was the outermost transaction, convert to final version
            if (!transactionManager.isInTransaction()) {
                convertToFinalVersion();
                clearPreTransactionState();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollbackTransaction() {
        if (transactionManager.isInTransaction()) {
            transactionManager.rollbackTransaction();

            // If this was the outermost transaction,
            // restore pre-transaction state
            if (!transactionManager.isInTransaction()
                && preTransactionState != null) {
                restoreFromPreTransactionState();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInTransaction() {
        return transactionManager.isInTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentStructure<T> getPreTransactionState() {
        return preTransactionState;
    }

    /**
     * Sets the pre-transaction state.
     *
     * @param preTransactionStateValue the state to save for rollback
     */
    protected void setPreTransactionState(
            final PersistentStructure<T> preTransactionStateValue) {
        this.preTransactionState = preTransactionStateValue;
    }

    /**
     * Saves the current state before transaction begins.
     * Must be implemented by subclasses.
     */
    protected abstract void savePreTransactionState();

    /**
     * Restores state from pre-transaction snapshot.
     * Must be implemented by subclasses.
     */
    protected abstract void restoreFromPreTransactionState();

    /**
     * Clears the pre-transaction state.
     */
    protected void clearPreTransactionState() {
        this.preTransactionState = null;
    }

    /**
     * Converts transactional versions to final versions.
     * Must be implemented by subclasses.
     */
    protected abstract void convertToFinalVersion();
}
