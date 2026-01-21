package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
@SuppressWarnings({"MagicNumber"})
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

  // ========== Basic Map Tests ==========

  @Test
  void testSize() {
    assertEquals(0, emptyMap.size());
    assertEquals(5, map.size());
  }

  @Test
  void testIsEmpty() {
    assertTrue(emptyMap.isEmpty());
    assertFalse(map.isEmpty());
  }

  @Test
  void testGet() {
    assertEquals(1, map.get("one"));
    assertEquals(2, map.get("two"));
    assertEquals(3, map.get("three"));
    assertEquals(4, map.get("four"));
    assertEquals(5, map.get("five"));
    assertNull(map.get("six"));
    assertNull(map.get(null));
  }

  @Test
  void testContainsKey() {
    assertTrue(map.containsKey("one"));
    assertTrue(map.containsKey("two"));
    assertTrue(map.containsKey("three"));
    assertTrue(map.containsKey("four"));
    assertTrue(map.containsKey("five"));
    assertFalse(map.containsKey("six"));
    assertFalse(map.containsKey(null));
  }

  @Test
  void testContainsValue() {
    assertTrue(map.containsValue(1));
    assertTrue(map.containsValue(2));
    assertTrue(map.containsValue(3));
    assertTrue(map.containsValue(4));
    assertTrue(map.containsValue(5));
    assertFalse(map.containsValue(6));
    assertFalse(map.containsValue(null));
  }

  @Test
  void testPutNewKey() {
    Integer oldValue = map.put("six", 6);
    assertNull(oldValue);
    assertEquals(6, map.size());
    assertEquals(6, map.get("six"));
    assertTrue(map.containsKey("six"));
  }

  @Test
  void testPutExistingKey() {
    Integer oldValue = map.put("three", 33);
    assertEquals(3, oldValue);
    assertEquals(5, map.size()); // Size unchanged
    assertEquals(33, map.get("three")); // Value updated
  }

  @Test
  void testPutNullKey() {
    Integer oldValue = map.put(null, 99);
    assertNull(oldValue);
    assertEquals(5, map.size()); // Should not add null key
    assertNull(map.get(null));
  }

  @Test
  void testRemove() {
    Integer oldValue = map.remove("three");
    assertEquals(3, oldValue);
    assertEquals(4, map.size());
    assertFalse(map.containsKey("three"));
    assertNull(map.get("three"));
  }

  @Test
  void testRemoveNonExistingKey() {
    Integer oldValue = map.remove("six");
    assertNull(oldValue);
    assertEquals(5, map.size());
  }

  @Test
  void testRemoveNullKey() {
    Integer oldValue = map.remove(null);
    assertNull(oldValue);
    assertEquals(5, map.size());
  }

  @Test
  void testPutAll() {
    Map<String, Integer> other = new HashMap<>();
    other.put("six", 6);
    other.put("seven", 7);
    other.put("one", 10); // Update existing

    map.putAll(other);

    assertEquals(7, map.size());
    assertEquals(10, map.get("one")); // Updated
    assertEquals(6, map.get("six")); // New
    assertEquals(7, map.get("seven")); // New
  }

  @Test
  void testClear() {
    assertFalse(map.isEmpty());
    map.clear();
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
  }

  @Test
  void testClearEmptyMap() {
    assertTrue(emptyMap.isEmpty());
    emptyMap.clear();
    assertTrue(emptyMap.isEmpty());
  }

  // ========== Tree-specific Methods ==========

  @Test
  void testFirstKey() {
    assertEquals("five", map.firstKey()); // Sorted order
  }

  @Test
  void testFirstKeyEmptyMap() {
    assertThrows(NoSuchElementException.class, emptyMap::firstKey);
  }

  @Test
  void testLastKey() {
    assertEquals("two", map.lastKey()); // Sorted order
  }

  @Test
  void testLastKeyEmptyMap() {
    assertThrows(NoSuchElementException.class, emptyMap::lastKey);
  }

  @Test
  void testHeight() {
    assertEquals(0, emptyMap.height());
    assertTrue(map.height() > 0);
  }

  @Test
  void testIsBalanced() {
    assertTrue(emptyMap.isBalanced());
    assertTrue(map.isBalanced());
  }

  // ========== EntrySet, KeySet, Values Tests ==========

  @Test
  void testEntrySet() {
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    assertEquals(5, entrySet.size());
    assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 1)));
  }

  @Test
  void testKeySet() {
    Set<String> keySet = map.keySet();
    assertEquals(5, keySet.size());
    assertTrue(keySet.contains("one"));
    assertFalse(keySet.contains("six"));
  }

  @Test
  void testValues() {
    Collection<Integer> values = map.values();
    assertEquals(5, values.size());
    assertTrue(values.contains(1));
    assertFalse(values.contains(6));
  }

  @Test
  void testEntrySetIterator() {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
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
  void testEntrySetIteratorRemove() {
    Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
    assertTrue(iterator.hasNext());
    Map.Entry<String, Integer> entry = iterator.next();
    
    // Remove via iterator should work
    iterator.remove();
    assertEquals(4, map.size());
    assertFalse(map.containsKey(entry.getKey()));
  }

  @Test
  void testEntrySetAdd() {
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    Map.Entry<String, Integer> newEntry = new AbstractMap.SimpleEntry<>("six", 6);
    
    assertTrue(entrySet.add(newEntry));
    assertEquals(6, map.size());
    assertEquals(6, map.get("six"));
  }

  @Test
  void testEntrySetRemove() {
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("one", 1);
    
    assertTrue(entrySet.remove(entry));
    assertEquals(4, map.size());
    assertFalse(map.containsKey("one"));
  }

  @Test
  void testEntrySetRemoveWrongValue() {
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    Map.Entry<String, Integer> wrongEntry = new AbstractMap.SimpleEntry<>("one", 999);
    
    assertFalse(entrySet.remove(wrongEntry));
    assertEquals(5, map.size());
    assertTrue(map.containsKey("one"));
  }

  // ========== Transactional-specific Methods ==========

  @Test
  void testSnapshot() {
    // Get snapshot
    PersistentTreeMap<String, Integer> snapshot = map.snapshot();

    // Modify transactional map
    map.put("six", 6);
    map.remove("two");

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
    TransactionalPersistentTreeMap<String, Integer> copy =
        map.transactionalCopy();

    // Should have same content initially
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

    // Modify copy independently
    copy.put("seven", 7);
    assertEquals(6, copy.size());
    assertTrue(copy.containsKey("seven"));
    assertFalse(map.containsKey("seven"));
  }

  @Test
  void testConcurrentModificationSafety() {
    Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
    
    // Modify map while iterating
    map.put("six", 6);
    
    // Should still work because iterator uses snapshot
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    assertEquals(5, count); // Original size, not new size
    
    // New iterator should see updated state
    Iterator<Map.Entry<String, Integer>> newIterator = map.entrySet().iterator();
    int newCount = 0;
    while (newIterator.hasNext()) {
      newIterator.next();
      newCount++;
    }
    assertEquals(6, newCount); // New size
  }

  // ========== Performance and Stress Tests ==========

  @Test
  void testLargeNumberOfEntries() {
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
  void testRandomOperations() {
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
          Integer treeOldValue = transactional.put(key, value);
          Integer refOldValue = reference.put(key, value);
          assertEquals(refOldValue, treeOldValue,
            "Put operation mismatch for key: " + key);
          break;

        case 1: // Remove
          Integer treeRemoved = transactional.remove(key);
          Integer refRemoved = reference.remove(key);
          assertEquals(refRemoved, treeRemoved,
            "Remove operation mismatch for key: " + key);
          break;

        case 2: // Get/Contains
          boolean treeContains = transactional.containsKey(key);
          boolean refContains = reference.containsKey(key);
          assertEquals(refContains, treeContains,
            "Contains mismatch for key: " + key);
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

  // ========== Edge Cases ==========

  @Test
  void testEmptyMapOperations() {
    assertTrue(emptyMap.isEmpty());
    assertEquals(0, emptyMap.size());
    assertNull(emptyMap.get("anything"));
    assertFalse(emptyMap.containsKey("anything"));
    assertFalse(emptyMap.containsValue(1));
    assertEquals("{}", emptyMap.toString());
    
    // Operations on empty map
    assertNull(emptyMap.put("key", 1));
    assertEquals(1, emptyMap.size());
    
    emptyMap.clear();
    assertTrue(emptyMap.isEmpty());
  }

  @Test
  void testSingleElementMap() {
    TransactionalPersistentTreeMap<String, Integer> single = 
        new TransactionalPersistentTreeMap<>();
    
    assertNull(single.put("key", 42));
    assertEquals(1, single.size());
    assertEquals(42, single.get("key"));
    assertTrue(single.containsKey("key"));
    assertTrue(single.containsValue(42));
    assertEquals("{key=42}", single.toString());
    
    assertEquals(42, single.remove("key"));
    assertTrue(single.isEmpty());
  }

  @Test
  void testClassCastExceptionHandling() {
    // These should not throw ClassCastException
    assertNull(map.get(123)); // Wrong type for key
    assertFalse(map.containsKey(123));
    assertFalse(map.containsValue("wrong type"));
    
    // Remove with wrong type should return null
    assertNull(map.remove(123));
  }

  @Test
  void testToString() {
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

  @Test
  void testEqualsAndHashCode() {
    TransactionalPersistentTreeMap<String, Integer> map1 =
        new TransactionalPersistentTreeMap<>();
    map1.put("a", 1);
    map1.put("b", 2);
    map1.put("c", 3);

    TransactionalPersistentTreeMap<String, Integer> map2 =
        new TransactionalPersistentTreeMap<>();
    map2.put("c", 3);
    map2.put("a", 1);
    map2.put("b", 2);

    // Different insertion order, same elements - should be equal
    assertEquals(map1, map2);
    assertEquals(map1.hashCode(), map2.hashCode());

    // Compare with standard map
    Map<String, Integer> standardMap = new java.util.TreeMap<>();
    standardMap.put("a", 1);
    standardMap.put("b", 2);
    standardMap.put("c", 3);
    
    assertEquals(standardMap, map1);
    assertEquals(map1, standardMap);
  }

  @Test
  void testEntrySetOperations() {
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    
    // Test containsAll
    Collection<Map.Entry<String, Integer>> entries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("one", 1),
            new AbstractMap.SimpleEntry<>("two", 2));
    assertTrue(entrySet.containsAll(entries));
    
    // Test addAll
    Collection<Map.Entry<String, Integer>> newEntries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("six", 6),
            new AbstractMap.SimpleEntry<>("seven", 7));
    assertTrue(entrySet.addAll(newEntries));
    assertEquals(7, map.size());
    
    // Test retainAll
    Collection<Map.Entry<String, Integer>> retainEntries =
        Arrays.asList(
            new AbstractMap.SimpleEntry<>("one", 1),
            new AbstractMap.SimpleEntry<>("two", 2));
    assertTrue(entrySet.retainAll(retainEntries));
    assertEquals(2, map.size());
    assertTrue(map.containsKey("one"));
    assertTrue(map.containsKey("two"));
    
    // Test removeAll
    Collection<Map.Entry<String, Integer>> removeEntries =
        Arrays.asList(new AbstractMap.SimpleEntry<>("one", 1));
    assertTrue(entrySet.removeAll(removeEntries));
    assertEquals(1, map.size());
    assertFalse(map.containsKey("one"));
    assertTrue(map.containsKey("two"));
  }
}

// Helper class for NoSuchElementException
class NoSuchElementExceptionException extends RuntimeException {
  public NoSuchElementExceptionException(String message) {
    super(message);
  }
}
