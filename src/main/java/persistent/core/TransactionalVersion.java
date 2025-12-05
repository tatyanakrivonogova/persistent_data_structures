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
     * @param transactionIdValue identifier of the transaction
     * @param transactionSequenceValue sequential number within transaction
     */
    public TransactionalVersion(final UUID transactionIdValue,
        final int transactionSequenceValue) {
        super();
        this.transactionId = transactionIdValue;
        this.isTransactional = true;
        this.transactionSequence = transactionSequenceValue;
    }

    /**
     * Creates a version from existing UUID with specified
     * transaction properties.
     *
     * @param id version identifier
     * @param transactionIdValue transaction identifier (can be null)
     * @param isTransactionalValue flag indicating transactional status
     * @param transactionSequenceValue sequential number within transaction
     */
    public TransactionalVersion(final UUID id, final UUID transactionIdValue,
        final boolean isTransactionalValue,
        final int transactionSequenceValue) {
        super(id);
        this.transactionId = transactionIdValue;
        this.isTransactional = isTransactionalValue;
        this.transactionSequence = transactionSequenceValue;
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
     * @return true if this version was created within a transaction,
     * false otherwise
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
            return base + "{transaction=" + transactionId
                + ", seq=" + transactionSequence + "}";
        }
        return base;
    }
}
