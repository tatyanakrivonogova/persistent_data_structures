package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent maps that provides mutable Map
 * interface. Implements the 6-step transaction process.
 *
 * @param <K> the type of keys, must be Comparable
 * @param <V> the type of values
 */
public final class TransactionalPersistentTreeMap<K extends Comparable<K>, V>
    implements Map<K, V> {

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

    for (Map.Entry<K, V> entry : map1.entrySet()) {
      V value2 = map2.get(entry.getKey());
      if (!Objects.equals(entry.getValue(), value2)) {
        return false;
      }
    }

    return true;
  }

  // ========== Map interface methods ==========

  @Override
  public int size() {
    return currentRef.get().size();
  }

  @Override
  public boolean isEmpty() {
    return currentRef.get().isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return currentRef.get().containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return currentRef.get().containsValue(value);
  }

  @Override
  public V get(final Object key) {
    return currentRef.get().get(key);
  }

  @Override
  public V put(final K key, final V value) {
    if (key == null) {
      return null; // Don't allow null keys
    }

    V oldValue = get(key);
    modify(map -> map.putInternal(key, value));
    return oldValue;
  }

  @Override
  public V remove(final Object key) {
    try {
      @SuppressWarnings("unchecked")
      K k = (K) key;
      if (k == null) {
        return null; // Can't remove null key
      }

      V oldValue = get(k);
      modify(map -> map.removeInternal(k));
      return oldValue;
    } catch (ClassCastException e) {
      return null;
    }
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    modify(map -> {
      PersistentTreeMap<K, V> result = map;
      for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
        if (entry.getKey() != null) {
          result = result.putInternal(entry.getKey(), entry.getValue());
        }
      }
      return result;
    });
  }

  @Override
  public void clear() {
    modify(map -> new PersistentTreeMap<>());
  }

  @Override
  public Set<K> keySet() {
    return currentRef.get().keySet();
  }

  @Override
  public Collection<V> values() {
    return currentRef.get().values();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  // ========== Transactional-specific methods ==========

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Map)) {
      return false;
    }
    return currentRef.get().equals(o);
  }

  @Override
  public int hashCode() {
    return currentRef.get().hashCode();
  }

  // ========== EntrySet implementation ==========

  private final class EntrySet implements Set<Map.Entry<K, V>> {
    @Override
    public int size() {
      return TransactionalPersistentTreeMap.this.size();
    }

    @Override
    public boolean isEmpty() {
      return TransactionalPersistentTreeMap.this.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      V value = TransactionalPersistentTreeMap.this.get(entry.getKey());
      return Objects.equals(value, entry.getValue());
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    @Override
    public Object[] toArray() {
      return currentRef.get().entrySet().toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
      return currentRef.get().entrySet().toArray(a);
    }

    @Override
    public boolean add(final Map.Entry<K, V> e) {
      V oldValue = put(e.getKey(), e.getValue());
      return !Objects.equals(oldValue, e.getValue());
    }

    @Override
    public boolean remove(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      V value = TransactionalPersistentTreeMap.this.get(entry.getKey());
      if (Objects.equals(value, entry.getValue())) {
        return TransactionalPersistentTreeMap.this.remove(entry.getKey())
          != null;
      }
      return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
      for (Object o : c) {
        if (!contains(o)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean addAll(
        final Collection<? extends Map.Entry<K, V>> c) {
      boolean changed = false;
      for (Map.Entry<K, V> entry : c) {
        if (add(entry)) {
          changed = true;
        }
      }
      return changed;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      boolean changed = false;
      Iterator<Map.Entry<K, V>> it = iterator();
      while (it.hasNext()) {
        if (!c.contains(it.next())) {
          it.remove();
          changed = true;
        }
      }
      return changed;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      boolean changed = false;
      for (Object o : c) {
        if (remove(o)) {
          changed = true;
        }
      }
      return changed;
    }

    @Override
    public void clear() {
      TransactionalPersistentTreeMap.this.clear();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Set)) {
        return false;
      }
      Set<?> that = (Set<?>) o;
      if (size() != that.size()) {
        return false;
      }
      return containsAll(that);
    }

    @Override
    public int hashCode() {
      int h = 0;
      for (Map.Entry<K, V> entry : this) {
        h += entry.hashCode();
      }
      return h;
    }
  }

  // ========== EntryIterator ==========

  private final class EntryIterator implements Iterator<Map.Entry<K, V>> {
    /**
     * Iterator over a snapshot of the map entries.
     */
    private final Iterator<Map.Entry<K, V>> snapshotIterator;

    /**
     * The last entry returned by the iterator.
     */
    private Map.Entry<K, V> lastReturned;

    EntryIterator() {
      this.snapshotIterator = currentRef.get().entrySet().iterator();
      this.lastReturned = null;
    }

    @Override
    public boolean hasNext() {
      return snapshotIterator.hasNext();
    }

    @Override
    public Map.Entry<K, V> next() {
      lastReturned = snapshotIterator.next();
      return lastReturned;
    }

    @Override
    public void remove() {
      if (lastReturned == null) {
        throw new IllegalStateException();
      }
      TransactionalPersistentTreeMap.this.remove(lastReturned.getKey());
      lastReturned = null;
    }
  }
}
