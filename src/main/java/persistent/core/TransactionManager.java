package persistent.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер транзакций для персистентных структур.
 * Управляет стеком транзакций и их состояниями.
 *
 * @version 3.0
 */
public class TransactionManager {
    /**
     * Состояние транзакции.
     */
    private static class TransactionState {
        final UUID transactionId;
        int sequenceCounter;
        final Map<String, Object> context;
        
        TransactionState(UUID transactionId) {
            this.transactionId = transactionId;
            this.sequenceCounter = 0;
            this.context = new HashMap<>();
        }
        
        int getNextSequence() {
            return ++sequenceCounter;
        }
    }
    
    private final Deque<TransactionState> transactionStack;
    private final ThreadLocal<TransactionState> currentTransaction;
    
    public TransactionManager() {
        this.transactionStack = new ArrayDeque<>();
        this.currentTransaction = new ThreadLocal<>();
    }
    
    /**
     * Начинает новую транзакцию.
     */
    public UUID beginTransaction() {
        UUID transactionId = UUID.randomUUID();
        TransactionState state = new TransactionState(transactionId);
        transactionStack.push(state);
        currentTransaction.set(state);
        return transactionId;
    }
    
    /**
     * Подтверждает текущую транзакцию.
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
     * Откатывает текущую транзакцию.
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
     * Проверяет, активна ли транзакция.
     */
    public boolean isInTransaction() {
        return !transactionStack.isEmpty();
    }
    
    /**
     * Возвращает идентификатор текущей транзакции.
     */
    public UUID getCurrentTransactionId() {
        TransactionState state = currentTransaction.get();
        return state != null ? state.transactionId : null;
    }
    
    /**
     * Получает следующий порядковый номер в текущей транзакции.
     */
    public int getNextTransactionSequence() {
        TransactionState state = currentTransaction.get();
        return state != null ? state.getNextSequence() : 0;
    }
    
    /**
     * Сохраняет значение в контексте транзакции.
     */
    public void putInContext(String key, Object value) {
        TransactionState state = currentTransaction.get();
        if (state != null) {
            state.context.put(key, value);
        }
    }
    
    /**
     * Получает значение из контекста транзакции.
     */
    public Object getFromContext(String key) {
        TransactionState state = currentTransaction.get();
        return state != null ? state.context.get(key) : null;
    }
    
    /**
     * Очищает контекст транзакции.
     */
    public void clearContext() {
        TransactionState state = currentTransaction.get();
        if (state != null) {
            state.context.clear();
        }
    }
}