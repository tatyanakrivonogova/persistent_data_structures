package persistent.core;

import java.util.UUID;

/**
 * Version with transaction support.
 * Extends SimpleVersion to add transaction metadata.
 *
 * @version 3.0
 */
public class TransactionalVersion extends SimpleVersion {
    /**
     * Transaction identifier (null if not in transaction).
     */
    private final UUID transactionId;
    
    /**
     * Flag indicating this is a temporary version within a transaction.
     */
    private final boolean isTransactional;
    
    /**
     * Sequential number within the transaction.
     */
    private final int transactionSequence;
    
    /**
     * Creates a new regular version (non-transactional).
     * Used for operations outside of transactions.
     */
    public TransactionalVersion() {
        super();
        this.transactionId = null;
        this.isTransactional = false;
        this.transactionSequence = 0;
    }
    
    /**
     * Creates a new transactional version.
     *
     * @param transactionId identifier of the transaction
     * @param transactionSequence sequential number within transaction
     */
    public TransactionalVersion(UUID transactionId, int transactionSequence) {
        super();
        this.transactionId = transactionId;
        this.isTransactional = true;
        this.transactionSequence = transactionSequence;
    }
    
    /**
     * Creates a version from existing UUID with specified transaction properties.
     *
     * @param id version identifier
     * @param transactionId transaction identifier (can be null)
     * @param isTransactional flag indicating transactional status
     * @param transactionSequence sequential number within transaction
     */
    public TransactionalVersion(UUID id, UUID transactionId, 
                               boolean isTransactional, int transactionSequence) {
        super(id);
        this.transactionId = transactionId;
        this.isTransactional = isTransactional;
        this.transactionSequence = transactionSequence;
    }
    
    /**
     * Returns the transaction identifier.
     *
     * @return transaction identifier, or null if not transactional
     */
    public UUID getTransactionId() {
        return transactionId;
    }
    
    /**
     * Checks if this version is transactional.
     *
     * @return true if this version was created within a transaction, false otherwise
     */
    public boolean isTransactional() {
        return isTransactional;
    }
    
    /**
     * Returns the sequential number within the transaction.
     *
     * @return transaction sequence number, or 0 if not transactional
     */
    public int getTransactionSequence() {
        return transactionSequence;
    }
    
    /**
     * Returns string representation of the version.
     * Includes transaction information for transactional versions.
     *
     * @return string representation of the version
     */
    @Override
    public String toString() {
        String base = super.toString();
        if (isTransactional) {
            return base + "{transaction=" + transactionId + 
                   ", seq=" + transactionSequence + "}";
        }
        return base;
    }
}
