package persistent.core;

/**
 * Interface for structures with transaction support.
 * Extends persistent structures with transaction management capabilities.
 *
 * @param <T> the type of elements in this tree, must be Comparable
 * @version 3.0
 */
public interface TransactionalStructure<T> extends PersistentStructure<T> {
    /**
     * Begins a new transaction.
     * All subsequent operations will be performed within this transaction.
     */
    void beginTransaction();

    /**
     * Commits the current transaction.
     * Applies all changes made during the transaction to the main structure.
     */
    void commitTransaction();

    /**
     * Rolls back the current transaction.
     * Discards all changes made during the transaction and restores
     * previous state.
     */
    void rollbackTransaction();

    /**
     * Checks if a transaction is currently active.
     *
     * @return true if a transaction is currently active, false otherwise
     */
    boolean isInTransaction();

    /**
     * Returns a snapshot of the state before the current transaction started.
     *
     * @return persistent structure representing the state before
     * transaction began,
     *         or current state if no transaction is active
     */
    PersistentStructure<T> getPreTransactionState();
}
