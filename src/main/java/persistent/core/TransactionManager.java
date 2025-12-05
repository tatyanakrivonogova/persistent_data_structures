package persistent.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction manager for persistent data structures.
 * Manages transaction stack and their states.
 * Provides transaction isolation and context management.
 *
 * @version 3.0
 */
public class TransactionManager {
    /**
     * Internal class representing transaction state.
     * Contains transaction identifier, sequence counter and context.
     */
    private static class TransactionState {
        /**
         * Unique transaction identifier
         */
        private final UUID transactionId;

        /**
         * Counter for generating sequential numbers within transaction
         */
        private int sequenceCounter;

        /**
         * Transaction context for storing arbitrary data
         */
        private final Map<String, Object> context;

        /**
         * Creates new transaction state with specified identifier.
         *
         * @param transactionIdValue unique transaction identifier
         */
        TransactionState(final UUID transactionIdValue) {
            this.transactionId = transactionIdValue;
            this.sequenceCounter = 0;
            this.context = new HashMap<>();
        }

        /**
         * Returns the next sequential number in current transaction.
         *
         * @return next sequential number (incremented by 1)
         */
        int getNextSequence() {
            return ++sequenceCounter;
        }
    }

    /**
     * Stack of active transactions
     */
    private final Deque<TransactionState> transactionStack;

    /**
     * Thread-local storage for current transaction state
     */
    private final ThreadLocal<TransactionState> currentTransaction;

    /**
     * Creates new transaction manager instance.
     */
    public TransactionManager() {
        this.transactionStack = new ArrayDeque<>();
        this.currentTransaction = new ThreadLocal<>();
    }

    /**
     * Starts a new transaction.
     *
     * @return unique identifier of created transaction
     */
    public UUID beginTransaction() {
        UUID transactionId = UUID.randomUUID();
        TransactionState state = new TransactionState(transactionId);
        transactionStack.push(state);
        currentTransaction.set(state);
        return transactionId;
    }

    /**
     * Commits the current transaction.
     * Removes current transaction from stack and updates thread-local state.
     */
    public void commitTransaction() {
        if (!transactionStack.isEmpty()) {
            transactionStack.pop();
            if (transactionStack.isEmpty()) {
                currentTransaction.remove();
            } else {
                currentTransaction.set(transactionStack.peek());
            }
        }
    }

    /**
     * Rolls back the current transaction.
     * Removes current transaction from stack and updates thread-local state.
     */
    public void rollbackTransaction() {
        if (!transactionStack.isEmpty()) {
            transactionStack.pop();
            if (transactionStack.isEmpty()) {
                currentTransaction.remove();
            } else {
                currentTransaction.set(transactionStack.peek());
            }
        }
    }

    /**
     * Checks if transaction is currently active.
     *
     * @return true if there is an active transaction, false otherwise
     */
    public boolean isInTransaction() {
        return !transactionStack.isEmpty();
    }

    /**
     * Returns identifier of current transaction.
     *
     * @return current transaction identifier, or null if no active transaction
     */
    public UUID getCurrentTransactionId() {
        TransactionState state = currentTransaction.get();
        return state != null ? state.transactionId : null;
    }

    /**
     * Gets next sequential number in current transaction.
     *
     * @return next sequential number, or 0 if no active transaction
     */
    public int getNextTransactionSequence() {
        TransactionState state = currentTransaction.get();
        return state != null ? state.getNextSequence() : 0;
    }

    /**
     * Stores value in transaction context.
     *
     * @param key the key with which the value is to be associated
     * @param value the value to be stored
     */
    public void putInContext(final String key, final Object value) {
        TransactionState state = currentTransaction.get();
        if (state != null) {
            state.context.put(key, value);
        }
    }

    /**
     * Retrieves value from transaction context.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with specified key,
     * or null if no active transaction or key not found
     */
    public Object getFromContext(final String key) {
        TransactionState state = currentTransaction.get();
        return state != null ? state.context.get(key) : null;
    }

    /**
     * Clears transaction context.
     * Removes all key-value pairs from current transaction context.
     */
    public void clearContext() {
        TransactionState state = currentTransaction.get();
        if (state != null) {
            state.context.clear();
        }
    }
}
