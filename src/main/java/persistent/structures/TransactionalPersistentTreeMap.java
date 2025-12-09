package persistent.structures;

import persistent.core.PersistentStructure;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent maps that provides mutable Collection interface.
 * Implements the 6-step transaction process.
 */
public class TransactionalPersistentTreeMap<K extends Comparable<K>, V> 
        implements Collection<Map.Entry<K, V>> {
    
    private final AtomicReference<PersistentTreeMap<K, V>> currentRef;
    
    public TransactionalPersistentTreeMap() {
        this.currentRef = new AtomicReference<>(new PersistentTreeMap<>());
    }
    
    public TransactionalPersistentTreeMap(PersistentTreeMap<K, V> initial) {
        this.currentRef = new AtomicReference<>(initial);
    }
    
    /**
     * Executes a modification operation atomically.
     */
    private boolean modify(java.util.function.Function<PersistentTreeMap<K, V>, 
                           PersistentTreeMap<K, V>> operation) {
        while (true) {
            PersistentTreeMap<K, V> current = currentRef.get();
            PersistentTreeMap<K, V> newVersion = operation.apply(current);
            
            if (newVersion == current) {
                return false; // No changes
            }
            
            if (currentRef.compareAndSet(current, newVersion)) {
                return true; // Success
            }
            // Retry on conflict
        }
    }
    
    // ========== Map-specific operations ==========
    
    /**
     * Associates the specified value with the specified key.
     */
    public boolean put(K key, V value) {
        return modify(map -> map.put(key, value));
    }
    
    /**
     * Removes the mapping for the specified key.
     */
    public boolean removeKey(K key) {
        return modify(map -> map.remove(key));
    }
    
    /**
     * Returns the value to which the specified key is mapped.
     */
    public V get(K key) {
        return currentRef.get().get(key);
    }
    
    /**
     * Returns true if this map contains the specified key.
     */
    public boolean containsKey(K key) {
        return currentRef.get().containsKey(key);
    }
    
    /**
     * Returns true if this map contains the specified value.
     */
    public boolean containsValue(V value) {
        return currentRef.get().containsValue(value);
    }
    
    /**
     * Returns the first (lowest) key.
     */
    public K firstKey() {
        return currentRef.get().firstKey();
    }
    
    /**
     * Returns the last (highest) key.
     */
    public K lastKey() {
        return currentRef.get().lastKey();
    }
    
    // ========== Collection interface methods ==========
    
    @Override
    public boolean add(Map.Entry<K, V> entry) {
        return put(entry.getKey(), entry.getValue());
    }
    
    @Override
    public boolean remove(Object o) {
        try {
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            return removeKey(entry.getKey());
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public boolean contains(Object o) {
        return currentRef.get().contains(o);
    }
    
    @Override
    public int size() {
        return currentRef.get().size();
    }
    
    @Override
    public boolean isEmpty() {
        return currentRef.get().isEmpty();
    }
    
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return currentRef.get().iterator();
    }
    
    @Override
    public Object[] toArray() {
        return currentRef.get().toArray();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        return currentRef.get().toArray(a);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return currentRef.get().containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        return modify(map -> {
            PersistentTreeMap<K, V> result = map;
            for (Map.Entry<K, V> entry : c) {
                result = result.put(entry.getKey(), entry.getValue());
            }
            return result;
        });
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return modify(map -> {
            PersistentTreeMap<K, V> result = map;
            for (Object obj : c) {
                try {
                    @SuppressWarnings("unchecked")
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
                    result = result.remove(entry.getKey());
                } catch (ClassCastException e) {
                    // Skip elements of wrong type
                }
            }
            return result;
        });
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return modify(map -> {
            PersistentTreeMap<K, V> result = new PersistentTreeMap<>();
            for (Map.Entry<K, V> entry : map) {
                if (c.contains(entry)) {
                    result = result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        });
    }
    
    @Override
    public void clear() {
        modify(map -> new PersistentTreeMap<>());
    }
    
    /**
     * Returns the current immutable snapshot.
     */
    public PersistentTreeMap<K, V> snapshot() {
        return currentRef.get();
    }
    
    /**
     * Returns a transactional wrapper for the current snapshot.
     */
    public TransactionalPersistentTreeMap<K, V> transactionalCopy() {
        return new TransactionalPersistentTreeMap<>(currentRef.get());
    }
    
    // ========== Utility methods ==========
    
    public int height() {
        return currentRef.get().height();
    }
    
    public boolean isBalanced() {
        return currentRef.get().isBalanced();
    }
    
    @Override
    public String toString() {
        return currentRef.get().toString();
    }
}