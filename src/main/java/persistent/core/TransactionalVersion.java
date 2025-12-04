package persistent.core;

import java.util.UUID;

/**
 * Версия с поддержкой транзакционности.
 * Расширяет SimpleVersion для добавления метаданных о транзакциях.
 *
 * @version 3.0
 */
public class TransactionalVersion extends SimpleVersion {
    /**
     * Идентификатор транзакции (null, если не в транзакции).
     */
    private final UUID transactionId;
    
    /**
     * Флаг, указывающий, что это временная версия в транзакции.
     */
    private final boolean isTransactional;
    
    /**
     * Порядковый номер внутри транзакции.
     */
    private final int transactionSequence;
    
    /**
     * Создает новую обычную версию (не транзакционную).
     */
    public TransactionalVersion() {
        super();
        this.transactionId = null;
        this.isTransactional = false;
        this.transactionSequence = 0;
    }
    
    /**
     * Создает новую транзакционную версию.
     */
    public TransactionalVersion(UUID transactionId, int transactionSequence) {
        super();
        this.transactionId = transactionId;
        this.isTransactional = true;
        this.transactionSequence = transactionSequence;
    }
    
    /**
     * Создает версию из существующего UUID с указанием транзакционных свойств.
     */
    public TransactionalVersion(UUID id, UUID transactionId, 
                               boolean isTransactional, int transactionSequence) {
        super(id);
        this.transactionId = transactionId;
        this.isTransactional = isTransactional;
        this.transactionSequence = transactionSequence;
    }
    
    public UUID getTransactionId() {
        return transactionId;
    }
    
    public boolean isTransactional() {
        return isTransactional;
    }
    
    public int getTransactionSequence() {
        return transactionSequence;
    }
    
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