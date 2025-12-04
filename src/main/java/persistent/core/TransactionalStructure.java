package persistent.core;

/**
 * Интерфейс для структур с поддержкой транзакционности.
 *
 * @version 3.0
 */
public interface TransactionalStructure<T> extends PersistentStructure<T> {
    /**
     * Начинает новую транзакцию.
     */
    void beginTransaction();
    
    /**
     * Подтверждает текущую транзакцию.
     */
    void commitTransaction();
    
    /**
     * Откатывает текущую транзакцию.
     */
    void rollbackTransaction();
    
    /**
     * Проверяет, активна ли транзакция.
     */
    boolean isInTransaction();
    
    /**
     * Возвращает снимок состояния до начала текущей транзакции.
     */
    PersistentStructure<T> getPreTransactionState();
}