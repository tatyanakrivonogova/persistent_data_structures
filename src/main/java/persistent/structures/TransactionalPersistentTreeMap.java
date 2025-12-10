package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent maps that provides mutable Collection
 * interface. Implements the 6-step transaction process.
 *
 * @param <K> the type of keys, must be Comparable
 * @param <V> the type of values
 */
public final class TransactionalPersistentTreeMap<K extends Comparable<K>, V>
    implements Collection<Map.Entry<K, V>> {

  /** Atomic reference to the current version of the map. */
  private final AtomicReference<PersistentTreeMap<K, V>> currentRef;

  /** Creates a new empty transactional map. */
  public TransactionalPersistentTreeMap() {
    this.currentRef = new AtomicReference<>(new PersistentTreeMap<>());
  }

  /**
   * Creates a transactional map with the given initial state.
   *
   * @param initial the initial map state
   */
  public TransactionalPersistentTreeMap(
      final PersistentTreeMap<K, V> initial) {
    this.currentRef = new AtomicReference<>(initial);
  }

  /**
   * Executes a modification operation atomically.
   *
   * @param operation the function to apply to the map
   * @return true if the operation succeeded and changed the map
   */
  private boolean modify(
      final java.util.function.Function<PersistentTreeMap<K, V>,
      PersistentTreeMap<K, V>> operation) {
    while (true) {
      PersistentTreeMap<K, V> current = currentRef.get();
      PersistentTreeMap<K, V> newVersion = operation.apply(current);

      // Compare by content, not by reference
      if (newVersion.size() == current.size()
          && mapsEqual(newVersion, current)) {
        return false; // No changes
      }

      if (currentRef.compareAndSet(current, newVersion)) {
        return true; // Success
      }
      // Retry on conflict
    }
  }

  /**
   * Compares two PersistentTreeMaps by content.
   *
   * @param map1 the first map
   * @param map2 the second map
   * @return true if the maps are equal in content
   */
  private boolean mapsEqual(final PersistentTreeMap<K, V> map1,
      final PersistentTreeMap<K, V> map2) {
    if (map1.size() != map2.size()) {
      return false;
    }

    for (Map.Entry<K, V> entry : map1) {
      V value2 = map2.get(entry.getKey());
      if (!Objects.equals(entry.getValue(), value2)) {
        return false;
      }
    }

    return true;
  }

  // ========== Map-specific operations ==========

  /**
   * Associates the specified value with the specified key.
   *
   * @param key the key
   * @param value the value
   * @return true if the map changed as a result of this operation
   */
  public boolean put(final K key, final V value) {
    if (key == null) {
      return false; // Don't allow null keys
    }
    return modify(map -> map.put(key, value));
  }

  /**
   * Removes the mapping for the specified key.
   *
   * @param key the key to remove
   * @return true if the map contained the key
   */
  public boolean removeKey(final K key) {
    if (key == null) {
      return false; // Can't remove null key
    }
    return modify(map -> map.remove(key));
  }

  /**
   * Returns the value to which the specified key is mapped.
   *
   * @param key the key
   * @return the value associated with the key, or null if not found
   */
  public V get(final K key) {
    if (key == null) {
      return null;
    }
    return currentRef.get().get(key);
  }

  /**
   * Returns true if this map contains the specified key.
   *
   * @param key the key
   * @return true if the map contains the key
   */
  public boolean containsKey(final K key) {
    if (key == null) {
      return false;
    }
    return currentRef.get().containsKey(key);
  }

  /**
   * Returns true if this map contains the specified value.
   *
   * @param value the value
   * @return true if the map contains the value
   */
  public boolean containsValue(final V value) {
    return currentRef.get().containsValue(value);
  }

  /**
   * Returns the first (lowest) key.
   *
   * @return the first key
   * @throws NoSuchElementException if the map is empty
   */
  public K firstKey() {
    return currentRef.get().firstKey();
  }

  /**
   * Returns the last (highest) key.
   *
   * @return the last key
   * @throws NoSuchElementException if the map is empty
   */
  public K lastKey() {
    return currentRef.get().lastKey();
  }

  // ========== Collection interface methods ==========

  @Override
  public boolean add(final Map.Entry<K, V> entry) {
    if (entry == null || entry.getKey() == null) {
      return false; // Don't add null entries or null keys
    }
    return put(entry.getKey(), entry.getValue());
  }

  @Override
  public boolean remove(final Object o) {
    if (o == null) {
      return false;
    }
    try {
      @SuppressWarnings("unchecked")
      Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
      if (entry.getKey() == null) {
        return false;
      }

      // Check if entry exists before removal
      PersistentTreeMap<K, V> current = currentRef.get();
      V value = current.get(entry.getKey());
      if (value == null || !Objects.equals(value, entry.getValue())) {
        return false; // Entry doesn't exist or values don't match
      }

      return removeKey(entry.getKey());
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public boolean contains(final Object o) {
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
  public <T> T[] toArray(final T[] a) {
    return currentRef.get().toArray(a);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    for (Object element : c) {
      if (!contains(element)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends Map.Entry<K, V>> c) {
    return modify(
        map -> {
          PersistentTreeMap<K, V> result = map;
          for (Map.Entry<K, V> entry : c) {
            if (entry != null && entry.getKey() != null) {
              result = result.put(entry.getKey(), entry.getValue());
            }
          }
          return result;
        });
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    boolean changed = false;
    for (Object obj : c) {
      if (remove(obj)) {
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return modify(
        map -> {
          PersistentTreeMap<K, V> result = new PersistentTreeMap<>();
          boolean changed = false;

          for (Map.Entry<K, V> entry : map) {
            if (c.contains(entry)) {
              result = result.put(entry.getKey(), entry.getValue());
            } else {
              changed = true;
            }
          }

          return changed ? result : map;
        });
  }

  @Override
  public void clear() {
    modify(map -> new PersistentTreeMap<>());
  }

  /**
   * Returns the current immutable snapshot.
   *
   * @return the current map snapshot
   */
  public PersistentTreeMap<K, V> snapshot() {
    return currentRef.get();
  }

  /**
   * Returns a transactional wrapper for the current snapshot.
   *
   * @return a new transactional wrapper for the current map
   */
  public TransactionalPersistentTreeMap<K, V> transactionalCopy() {
    return new TransactionalPersistentTreeMap<>(currentRef.get());
  }

  // ========== Utility methods ==========

  /**
   * Returns the height of this tree map.
   *
   * @return the height of the tree
   */
  public int height() {
    return currentRef.get().height();
  }

  /**
   * Checks if this tree map is balanced.
   *
   * @return true if the tree is balanced, false otherwise
   */
  public boolean isBalanced() {
    return currentRef.get().isBalanced();
  }

  @Override
  public String toString() {
    return currentRef.get().toString();
  }
}
