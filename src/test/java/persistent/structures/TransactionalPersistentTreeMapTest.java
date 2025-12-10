package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Comprehensive test suite for TransactionalPersistentTreeMap. */
class TransactionalPersistentTreeMapTest {

  /** Empty map for testing. */
  private TransactionalPersistentTreeMap<String, Integer> emptyMap;
  
  /** Pre-populated map for testing. */
  private TransactionalPersistentTreeMap<String, Integer> map;

  @BeforeEach
  void setUp() {
    emptyMap = new TransactionalPersistentTreeMap<>();

    map = new TransactionalPersistentTreeMap<>();
    map.put("one", 1);
    map.put("two", 2);
    map.put("three", 3);
    map.put("four", 4);
    map.put("five", 5);
  }

  // ========== Map-specific Tests ==========

  @Test
  void testTransactionalPut() {
    TransactionalPersistentTreeMap<String, Integer> transactional =
        new TransactionalPersistentTreeMap<>();

    // Test single put
    assertTrue(transactional.put("a", 1));
    assertEquals(1, transactional.size());
    assertEquals(1, transactional.get("a"));

    // Test multiple puts
    assertTrue(transactional.put("b", 2));
    assertTrue(transactional.put("c", 3));
    assertEquals(3, transactional.size());
    assertEquals(2, transactional.get("b"));
    assertEquals(3, transactional.get("c"));

    // Test updating existing key
    assertTrue(transactional.put("a", 10));
    assertEquals(3, transactional.size()); // Size unchanged
    assertEquals(10, transactional.get("a")); // Value updated
  }

  @Test
  void testTransactionalPutNullKey() {
    // Should handle null key gracefully
    assertFalse(emptyMap.put(null, 99));
    assertEquals(0, emptyMap.size());
    assertNull(emptyMap.get(null));
  }

  @Test
  void testTransactionalRemoveKey() {
    // Test remove existing key
    assertTrue(map.removeKey("three"));
    assertEquals(4, map.size());
    assertFalse(map.containsKey("three"));
    assertNull(map.get("three"));

    // Test remove non-existing key
    assertFalse(map.removeKey("six"));
    assertEquals(4, map.size());

    // Test remove null key
    assertFalse(map.removeKey(null));
    assertEquals(4, map.size());
  }

  @Test
  void testTransactionalGet() {
    assertEquals(1, map.get("one"));
    assertEquals(2, map.get("two"));
    assertEquals(3, map.get("three"));
    assertEquals(4, map.get("four"));
    assertEquals(5, map.get("five"));
    assertNull(map.get("six"));
    assertNull(map.get(null));
  }

  @Test
  void testTransactionalContainsKey() {
    assertTrue(map.containsKey("one"));
    assertTrue(map.containsKey("two"));
    assertTrue(map.containsKey("three"));
    assertTrue(map.containsKey("four"));
    assertTrue(map.containsKey("five"));
    assertFalse(map.containsKey("six"));
    assertFalse(map.containsKey(null));
  }

  @Test
  void testTransactionalContainsValue() {
    assertTrue(map.containsValue(1));
    assertTrue(map.containsValue(2));
    assertTrue(map.containsValue(3));
    assertTrue(map.containsValue(4));
    assertTrue(map.containsValue(5));
    assertFalse(map.containsValue(6));
    assertFalse(map.containsValue(null));
  }

  @Test
  void testTransactionalFirstKey() {
    assertEquals("five", map.firstKey()); // Sorted order
  }

  @Test
  void testTransactionalFirstKeyEmptyMap() {
    assertThrows(NoSuchElementException.class, emptyMap::firstKey);
  }

  @Test
  void testTransactionalLastKey() {
    assertEquals("two", map.lastKey()); // Sorted order
  }

  @Test
  void testTransactionalLastKeyEmptyMap() {
    assertThrows(NoSuchElementException.class, emptyMap::lastKey);
  }

  @Test
  void testTransactionalHeight() {
    assertEquals(0, emptyMap.height());
    assertTrue(map.height() > 0);
  }

  @Test
  void testTransactionalIsBalanced() {
    assertTrue(emptyMap.isBalanced());
    assertTrue(map.isBalanced());
  }

  // ========== Collection Interface Tests ==========

  @Test
  void testTransactionalAdd() {
    TransactionalPersistentTreeMap<String, Integer> transactional =
        new TransactionalPersistentTreeMap<>();

    Map.Entry<String, Integer> entry1 = new AbstractMap.SimpleEntry<>("a", 1);
    Map.Entry<String, Integer> entry2 = new AbstractMap.SimpleEntry<>("b", 2);

    assertTrue(transactional.add(entry1));
    assertEquals(1, transactional.size());
    assertEquals(1, transactional.get("a"));

    assertTrue(transactional.add(entry2));
    assertEquals(2, transactional.size());
    assertEquals(2, transactional.get("b"));

    // Add duplicate key with different value (should update)
    Map.Entry<String, Integer> entry1Updated = 
        new AbstractMap.SimpleEntry<>("a", 10);
    assertTrue(transactional.add(entry1Updated));
    assertEquals(2, transactional.size()); // Size unchanged
    assertEquals(10, transactional.get("a")); // Value updated
  }

  @Test
  void testTransactionalRemoveEntry() {
    Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("three", 3);

    // Test remove existing entry
    assertTrue(map.remove(entry));
    assertEquals(4, map.size());
    assertFalse(map.containsKey("three"));

    // Test remove with wrong value
    Map.Entry<String, Integer> wrongValue = 
        new AbstractMap.SimpleEntry<>("two", 999);
    assertFalse(map.remove(wrongValue));
    assertEquals(4, map.size()); // Should not remove

    // Test remove non-existing key
    Map.Entry<String, Integer> nonExisting = 
        new AbstractMap.SimpleEntry<>("six", 6);
    assertFalse(map.remove(nonExisting));
    assertEquals(4, map.size());

    // Test remove with wrong type
    assertFalse(map.remove("not an entry"));
    assertEquals(4, map.size());
  }

  @Test
  void testTransactionalContains() {
    assertTrue(map.contains(new AbstractMap.SimpleEntry<>("one", 1)));
    assertTrue(map.contains(new AbstractMap.SimpleEntry<>("two", 2)));
    assertTrue(map.contains(new AbstractMap.SimpleEntry<>("three", 3)));
    assertFalse(map.contains(new AbstractMap.SimpleEntry<>("one", 2)));
    assertFalse(map.contains(new AbstractMap.SimpleEntry<>("six", 6)));
    assertFalse(map.contains("not an entry"));
    assertFalse(map.contains(null));
  }

  @Test
  void testTransactionalSize() {
    assertEquals(0, emptyMap.size());
    assertEquals(5, map.size());
  }

  @Test
  void testTransactionalIsEmpty() {
    assertTrue(emptyMap.isEmpty());
    assertFalse(map.isEmpty());
  }

  @Test
  void testTransactionalIterator() {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : map) {
      entries.add(entry);
    }

    assertEquals(5, entries.size());

    // Check keys are in sorted order
    List<String> keys = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : entries) {
      keys.add(entry.getKey());
    }
    assertEquals(Arrays.asList("five", "four", "one", "three", "two"), keys);
  }

  @Test
  void testTransactionalToArray() {
    Object[] array = map.toArray();
    assertEquals(5, array.length);

    // Convert to Set for easier checking
    Set<Map.Entry<String, Integer>> entrySet = new HashSet<>();
    for (Object obj : array) {
      @SuppressWarnings("unchecked")
      Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) obj;
      entrySet.add(entry);
    }

    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 1)));
    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("two", 2)));
    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("three", 3)));
    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("four", 4)));
    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("five", 5)));
  }

  @Test
  void testTransactionalContainsAll() {
    Collection<Map.Entry<String, Integer>> entries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("one", 1),
            new AbstractMap.SimpleEntry<>("two", 2),
            new AbstractMap.SimpleEntry<>("three", 3));

    assertTrue(map.containsAll(entries));

    Collection<Map.Entry<String, Integer>> mixedEntries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("one", 1),
            new AbstractMap.SimpleEntry<>("six", 6));

    assertFalse(map.containsAll(mixedEntries));

    // Empty collection should always return true
    assertTrue(map.containsAll(Collections.emptyList()));
  }

  @Test
  void testTransactionalAddAll() {
    TransactionalPersistentTreeMap<String, Integer> transactional =
        new TransactionalPersistentTreeMap<>();

    Collection<Map.Entry<String, Integer>> entries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("a", 1),
            new AbstractMap.SimpleEntry<>("b", 2),
            new AbstractMap.SimpleEntry<>("c", 3),
            new AbstractMap.SimpleEntry<>("a", 4));

    assertTrue(transactional.addAll(entries));
    assertEquals(3, transactional.size());
    assertEquals(4, transactional.get("a"));
    assertEquals(2, transactional.get("b"));
    assertEquals(3, transactional.get("c"));

    // Add empty collection
    assertFalse(transactional.addAll(Collections.emptyList()));
    assertEquals(3, transactional.size());
  }

  @Test
  void testTransactionalRemoveAll() {
    Collection<Map.Entry<String, Integer>> toRemove =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("two", 2),
            new AbstractMap.SimpleEntry<>("four", 4),
            new AbstractMap.SimpleEntry<>("six", 6));

    assertTrue(map.removeAll(toRemove));
    assertEquals(3, map.size());
    assertFalse(map.containsKey("two"));
    assertFalse(map.containsKey("four"));
    assertTrue(map.containsKey("one"));
    assertTrue(map.containsKey("three"));
    assertTrue(map.containsKey("five"));

    // Remove empty collection
    assertFalse(map.removeAll(Collections.emptyList()));
  }

  @Test
  void testTransactionalRetainAll() {
    Collection<Map.Entry<String, Integer>> toRetain =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("two", 2),
            new AbstractMap.SimpleEntry<>("four", 4),
            new AbstractMap.SimpleEntry<>("six", 6));

    assertTrue(map.retainAll(toRetain));
    assertEquals(2, map.size());
    assertTrue(map.containsKey("two"));
    assertTrue(map.containsKey("four"));
    assertFalse(map.containsKey("one"));
    assertFalse(map.containsKey("three"));
    assertFalse(map.containsKey("five"));

    // Retain all (no change)
    TransactionalPersistentTreeMap<String, Integer> copy = 
        new TransactionalPersistentTreeMap<>();
    copy.put("x", 1);
    copy.put("y", 2);

    Collection<Map.Entry<String, Integer>> allEntries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("x", 1),
            new AbstractMap.SimpleEntry<>("y", 2));

    assertFalse(copy.retainAll(allEntries));
    assertEquals(2, copy.size());

    // Retain none
    assertTrue(copy.retainAll(Collections.emptyList()));
    assertTrue(copy.isEmpty());
  }

  @Test
  void testTransactionalClear() {
    assertFalse(map.isEmpty());
    map.clear();
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
  }

  @Test
  void testTransactionalClearEmptyMap() {
    assertTrue(emptyMap.isEmpty());
    emptyMap.clear();
    assertTrue(emptyMap.isEmpty());
  }

  // ========== Persistence and Snapshot Tests ==========

  @Test
  void testTransactionalSnapshot() {
    // Get snapshot
    PersistentTreeMap<String, Integer> snapshot = map.snapshot();

    // Modify transactional map
    map.put("six", 6);
    map.removeKey("two");

    // Snapshot should remain unchanged
    assertEquals(5, snapshot.size());
    assertTrue(snapshot.containsKey("one"));
    assertTrue(snapshot.containsKey("two"));
    assertTrue(snapshot.containsKey("three"));
    assertTrue(snapshot.containsKey("four"));
    assertTrue(snapshot.containsKey("five"));
    assertFalse(snapshot.containsKey("six"));

    // Transactional map should have changes
    assertEquals(5, map.size());
    assertTrue(map.containsKey("six"));
    assertFalse(map.containsKey("two"));
  }

  @Test
  void testTransactionalCopy() {
    TransactionalPersistentTreeMap<String, Integer> copy = map.transactionalCopy();

    // Should have same content
    assertEquals(map.size(), copy.size());
    assertEquals(map.get("one"), copy.get("one"));
    assertEquals(map.get("two"), copy.get("two"));

    // Modify original
    map.put("six", 6);

    // Copy should remain unchanged
    assertEquals(5, copy.size());
    assertFalse(copy.containsKey("six"));

    // Original should have new entry
    assertEquals(6, map.size());
    assertTrue(map.containsKey("six"));
  }

  @Test
  void testConcurrentModificationSafety() {
    // Since we're using AtomicReference, we can safely iterate while modifying
    Iterator<Map.Entry<String, Integer>> iterator = map.iterator();
    map.put("six", 6); // Modify while iterating

    // Should still work because iterator uses snapshot
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    assertEquals(5, count); // Original size, not new size
  }

  // ========== Performance Tests ==========

  @Test
  void testTransactionalLargeNumberOfEntries() {
    TransactionalPersistentTreeMap<Integer, String> large = 
        new TransactionalPersistentTreeMap<>();
    int count = 1000;

    for (int i = 0; i < count; i++) {
      large.put(i, "Value" + i);
    }

    assertEquals(count, large.size());

    // Verify all entries are present
    for (int i = 0; i < count; i++) {
      assertEquals("Value" + i, large.get(i));
      assertTrue(large.containsKey(i));
    }

    // Verify tree is still balanced
    assertTrue(large.isBalanced());
  }

  @Test
  void testTransactionalRandomOperations() {
    Random random = new Random(42);
    TransactionalPersistentTreeMap<Integer, Integer> transactional =
        new TransactionalPersistentTreeMap<>();
    Map<Integer, Integer> reference = new HashMap<>();

    for (int i = 0; i < 1000; i++) {
      int operation = random.nextInt(3);
      int key = random.nextInt(100);
      int value = random.nextInt(1000);

      switch (operation) {
        case 0: // Put
          boolean treePut = transactional.put(key, value);
          Integer oldRefValue = reference.put(key, value);
          boolean refPut = oldRefValue == null || !oldRefValue.equals(value);
          assertEquals(refPut, treePut, "Put operation mismatch for key: " + key);
          break;

        case 1: // Remove
          boolean treeRemove = transactional.removeKey(key);
          boolean refRemove = reference.remove(key) != null;
          assertEquals(refRemove, treeRemove, "Remove operation mismatch for key: " + key);
          break;

        case 2: // Get/Contains
          boolean treeContains = transactional.containsKey(key);
          boolean refContains = reference.containsKey(key);
          assertEquals(refContains, treeContains, "Contains mismatch for key: " + key);
          break;
        default:
          // Handle unexpected operation
          break;
      }

      // Verify size matches
      assertEquals(reference.size(), transactional.size(), 
          "Size mismatch at iteration " + i);

      // Verify all reference entries are in tree
      for (Map.Entry<Integer, Integer> refEntry : reference.entrySet()) {
        assertEquals(
            refEntry.getValue(),
            transactional.get(refEntry.getKey()),
            "Value mismatch for key: " + refEntry.getKey());
      }
    }
  }

  @Test
  void testTransactionalToString() {
    TransactionalPersistentTreeMap<String, Integer> transactional =
        new TransactionalPersistentTreeMap<>();
    transactional.put("b", 2);
    transactional.put("a", 1);
    transactional.put("c", 3);

    String str = transactional.toString();
    // Should be in sorted order: a=1, b=2, c=3
    assertTrue(str.contains("a=1"));
    assertTrue(str.contains("b=2"));
    assertTrue(str.contains("c=3"));
    assertTrue(str.startsWith("{") && str.endsWith("}"));
  }
}
