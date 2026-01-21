package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Comprehensive test suite for PersistentTreeMapSlow. */
@SuppressWarnings({"MagicNumber", "LineLength", "LongLine", "MaxLineLength"})
class PersistentTreeMapSlowTest {

    /** Empty map for testing. */
    private PersistentTreeMapSlow<String, Integer> emptyMap;
    /** Map with elements for testing. */
    private PersistentTreeMapSlow<String, Integer> map;

    @BeforeEach
    void setUp() {
        emptyMap = new PersistentTreeMapSlow<>();

        // Build map using put operations
        PersistentTreeMapSlow<String, Integer> temp = new PersistentTreeMapSlow<>();
        temp = temp.putInternal("one", 1);
        temp = temp.putInternal("two", 2);
        temp = temp.putInternal("three", 3);
        temp = temp.putInternal("four", 4);
        temp = temp.putInternal("five", 5);
        map = temp;
    }

    // ========== Basic Map Tests ==========

    @Test
    void testIsEmpty() {
        assertTrue(emptyMap.isEmpty());
        assertFalse(map.isEmpty());
    }

    @Test
    void testSize() {
        assertEquals(0, emptyMap.size());
        assertEquals(5, map.size());
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
    void testGetInternal() {
        assertEquals(1, map.getInternal("one"));
        assertEquals(2, map.getInternal("two"));
        assertEquals(3, map.getInternal("three"));
        assertEquals(4, map.getInternal("four"));
        assertEquals(5, map.getInternal("five"));
        assertNull(map.getInternal("six"));
        assertNull(map.getInternal(null));
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
    void testPutInternalNewKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.putInternal("six", 6);

        assertEquals(6, newMap.size());
        assertEquals(6, newMap.get("six"));
        assertTrue(newMap.containsKey("six"));

        // Original map unchanged
        assertEquals(5, map.size());
        assertFalse(map.containsKey("six"));
    }

    @Test
    void testPutInternalExistingKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.putInternal("three", 33);

        assertEquals(5, newMap.size());
        assertEquals(33, newMap.get("three"));

        // Original map unchanged
        assertEquals(3, map.get("three"));
    }

    @Test
    void testPutInternalNullKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.putInternal(null, 99);

        // Should ignore null key
        assertEquals(5, newMap.size());
        assertNull(newMap.get(null));
    }

    @Test
    void testPutInternalSameValue() {
        // Putting same value should return same instance
        PersistentTreeMapSlow<String, Integer> newMap = map.putInternal("three", 3);
        assertSame(map, newMap);
    }

    @Test
    void testRemoveInternal() {
        PersistentTreeMapSlow<String, Integer> newMap = map.removeInternal("three");

        assertEquals(4, newMap.size());
        assertFalse(newMap.containsKey("three"));
        assertTrue(newMap.containsKey("one"));
        assertTrue(newMap.containsKey("two"));
        assertTrue(newMap.containsKey("four"));
        assertTrue(newMap.containsKey("five"));

        // Original map unchanged
        assertEquals(5, map.size());
        assertTrue(map.containsKey("three"));
    }

    @Test
    void testRemoveInternalNonExistingKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.removeInternal("six");

        // Should return same instance
        assertSame(map, newMap);
        assertEquals(5, newMap.size());
    }

    @Test
    void testRemoveInternalNullKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.removeInternal(null);

        // Should return same instance
        assertSame(map, newMap);
        assertEquals(5, newMap.size());
    }

    // ========== Map Interface Mutability Tests ==========

    @Test
    void testPutThrows() {
        assertThrows(UnsupportedOperationException.class, () -> map.put("six", 6));
    }

    @Test
    void testRemoveThrows() {
        assertThrows(UnsupportedOperationException.class, () -> map.remove("one"));
    }

    @Test
    void testClearThrows() {
        assertThrows(UnsupportedOperationException.class, map::clear);
    }

    @Test
    void testPutAllThrows() {
        Map<String, Integer> other = new HashMap<>();
        other.put("six", 6);
        assertThrows(UnsupportedOperationException.class, () -> map.putAll(other));
    }

    // ========== Collection Interface Tests ==========

    @Test
    void testIterator() {
        // Используем entrySet() для итерации
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            entries.add(entry);
        }

        assertEquals(5, entries.size());

        // Check keys are in sorted order
        List<String> keys = entries.stream().map(Map.Entry::getKey)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("five", "four", "one", "three", "two"), keys);

        // Check values
        List<Integer> values = entries.stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(5, 4, 1, 3, 2), values);
    }

    @Test
    void testIteratorEmptyMap() {
        Iterator<Map.Entry<String, Integer>> iterator = emptyMap.entrySet().iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorRemoveThrows() {
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testToArray() {
        // Используем entrySet().toArray()
        Object[] array = map.entrySet().toArray();
        assertEquals(5, array.length);

        // Convert to List of entries for easier checking
        List<Map.Entry<String, Integer>> entries =
                Arrays.stream(array)
                        .map(obj -> (Map.Entry<String, Integer>) obj)
                        .collect(Collectors.toList());

        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("one", 1)));
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("two", 2)));
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("three", 3)));
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("four", 4)));
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("five", 5)));
    }

    @Test
    void testToArrayWithType() {
        @SuppressWarnings("unchecked")
        Map.Entry<String, Integer>[] array = map.entrySet().toArray(new Map.Entry[0]);

        assertEquals(5, array.length);

        // Convert to Set for easier checking
        Set<Map.Entry<String, Integer>> entrySet = new HashSet<>(
                Arrays.asList(array));

        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 1)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("two", 2)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("three", 3)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("four", 4)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("five", 5)));
    }

    @Test
    void testContains() {
        // Используем entrySet().contains()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 1)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("two", 2)));
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("three", 3)));
        // Wrong value
        assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 2)));
        // Wrong key
        assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>("six", 6)));
        assertFalse(entrySet.contains("not an entry"));
        assertFalse(entrySet.contains(null));
    }

    @Test
    void testContainsAll() {
        // Используем entrySet().containsAll()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("two", 2),
                        new AbstractMap.SimpleEntry<>("three", 3));

        assertTrue(entrySet.containsAll(entries));

        Collection<Map.Entry<String, Integer>> mixedEntries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("six", 6) // Doesn't exist
                );

        assertFalse(entrySet.containsAll(mixedEntries));

        // Empty collection should always return true
        assertTrue(entrySet.containsAll(Collections.emptyList()));
    }

    @Test
    void testAddThrows() {
        // Используем entrySet().add()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("six", 6);
        assertThrows(UnsupportedOperationException.class, () -> entrySet.add(entry));
    }

    @Test
    void testRemoveEntryThrows() {
        // Используем entrySet().remove()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("one", 1);
        assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(entry));
    }

    @Test
    void testAddAllThrows() {
        // Используем entrySet().addAll()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("six", 6),
                        new AbstractMap.SimpleEntry<>("seven", 7));

        assertThrows(UnsupportedOperationException.class,
                () -> entrySet.addAll(entries));
    }

    @Test
    void testRemoveAllThrows() {
        // Используем entrySet().removeAll()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("two", 2));

        assertThrows(UnsupportedOperationException.class,
                () -> entrySet.removeAll(entries));
    }

    @Test
    void testRetainAllThrows() {
        // Используем entrySet().retainAll()
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 3),
                        new AbstractMap.SimpleEntry<>("two", 4));

        assertThrows(UnsupportedOperationException.class,
                () -> entrySet.retainAll(entries));
    }

    // ========== EntrySet, KeySet, Values Tests ==========

    @Test
    void testEntrySet() {
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        assertEquals(5, entrySet.size());
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("one", 1)));
        assertFalse(entrySet.contains("not an entry"));
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
        List<String> keys = entries.stream().map(Map.Entry::getKey)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("five", "four", "one", "three", "two"), keys);

        // Check values
        List<Integer> values = entries.stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(5, 4, 1, 3, 2), values);
    }

    @Test
    void testEntrySetIteratorEmptyMap() {
        Iterator<Map.Entry<String, Integer>> iterator = emptyMap.entrySet().iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testEntrySetIteratorRemoveThrows() {
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testKeySetIterator() {
        List<String> keys = new ArrayList<>();
        for (String key : map.keySet()) {
            keys.add(key);
        }
        
        assertEquals(Arrays.asList("five", "four", "one", "three", "two"), keys);
    }

    @Test
    void testValuesIterator() {
        List<Integer> values = new ArrayList<>();
        for (Integer value : map.values()) {
            values.add(value);
        }
        
        assertEquals(Arrays.asList(5, 4, 1, 3, 2), values);
    }

    // ========== PersistentStructure Interface Tests ==========

    @Test
    void testCreateWithAdded() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("six", 6);
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithAdded(entry);

        assertEquals(6, newMap.size());
        assertEquals(6, newMap.get("six"));

        // Original unchanged
        assertEquals(5, map.size());
        assertNull(map.get("six"));
    }

    @Test
    void testCreateWithAddedNull() {
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithAdded(null);
        assertSame(map, newMap);
    }

    @Test
    void testCreateWithAddedNullKey() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(null, 99);
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithAdded(entry);
        assertSame(map, newMap);
    }

    @Test
    void testCreateWithRemoved() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("one", 1);
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithRemoved(entry);

        assertEquals(4, newMap.size());
        assertFalse(newMap.containsKey("one"));

        // Original unchanged
        assertEquals(5, map.size());
        assertTrue(map.containsKey("one"));
    }

    @Test
    void testCreateWithRemovedNull() {
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithRemoved(null);
        assertSame(map, newMap);
    }

    @Test
    void testCreateWithRemovedNullKey() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(null, 99);
        PersistentTreeMapSlow<String, Integer> newMap =
                (PersistentTreeMapSlow<String, Integer>) map.createWithRemoved(entry);
        assertSame(map, newMap);
    }

    @Test
    void testCreateEmpty() {
        PersistentTreeMapSlow<String, Integer> empty =
                (PersistentTreeMapSlow<String, Integer>) map.createEmpty();

        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertNotSame(map, empty);
    }

    @Test
    void testContainsElement() {
        assertTrue(map.containsElement(new AbstractMap.SimpleEntry<>("one", 1)));
        // Wrong value
        assertFalse(map.containsElement(new AbstractMap.SimpleEntry<>("one", 2)));
        // Wrong key
        assertFalse(map.containsElement(new AbstractMap.SimpleEntry<>("six", 6)));
        // Null entry
        assertFalse(map.containsElement(null));
        // Null key
        assertFalse(map.containsElement(new AbstractMap.SimpleEntry<>(null, 99)));
    }

    @Test
    void testGetVersion() {
        assertNotNull(map.getVersion());
    }

    @Test
    void testSnapshot() {
        PersistentTreeMapSlow<String, Integer> snapshot =
                (PersistentTreeMapSlow<String, Integer>) map.snapshot();
        assertEquals(map.size(), snapshot.size());
        assertEquals(map.get("one"), snapshot.get("one"));
    }

    // ========== Persistence Tests ==========

    @Test
    void testImmutability() {
        // Create original map
        PersistentTreeMapSlow<String, Integer> original = new PersistentTreeMapSlow<>();
        original = original.putInternal("a", 1);
        original = original.putInternal("b", 2);

        // Create new version (original remains unchanged)
        PersistentTreeMapSlow<String, Integer> modified = original.putInternal("c", 3);

        // Modified has new entry
        assertEquals(3, modified.size());
        assertTrue(modified.containsKey("c"));

        // Original unchanged
        assertEquals(2, original.size());
        assertFalse(original.containsKey("c"));
    }

    @Test
    void testMultipleVersions() {
        // Create version chain
        PersistentTreeMapSlow<String, Integer> v1 = new PersistentTreeMapSlow<>();

        PersistentTreeMapSlow<String, Integer> v2 = v1.putInternal("a", 1);
        PersistentTreeMapSlow<String, Integer> v3 = v2.putInternal("b", 2);
        PersistentTreeMapSlow<String, Integer> v4 = v3.putInternal("c", 3);

        // Remove from middle version
        PersistentTreeMapSlow<String, Integer> v5 = v3.removeInternal("b");

        // All versions should be independent
        assertEquals(0, v1.size());
        assertEquals(1, v2.size());
        assertEquals(1, v2.get("a"));

        assertEquals(2, v3.size());
        assertEquals(1, v3.get("a"));
        assertEquals(2, v3.get("b"));

        assertEquals(3, v4.size());
        assertEquals(1, v4.get("a"));
        assertEquals(2, v4.get("b"));
        assertEquals(3, v4.get("c"));

        assertEquals(1, v5.size());
        assertEquals(1, v5.get("a"));
        assertFalse(v5.containsKey("b"));
    }

    // ========== Performance and Edge Cases ==========

    @Test
    void testLargeNumberOfEntries() {
        PersistentTreeMapSlow<Integer, String> large = new PersistentTreeMapSlow<>();
        int count = 1000;

        for (int i = 0; i < count; i++) {
            large = large.putInternal(i, "Value" + i);
        }

        assertEquals(count, large.size());

        // Verify all entries are present
        for (int i = 0; i < count; i++) {
            assertEquals("Value" + i, large.get(i));
            assertTrue(large.containsKey(i));
        }
    }

    @Test
    void testPerformanceMultipleVersions() {
        // Test that creating multiple versions doesn't cause performance issues
        PersistentTreeMapSlow<Integer, Integer> base = new PersistentTreeMapSlow<>();
        List<PersistentTreeMapSlow<Integer, Integer>> versions = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            base = base.putInternal(i, i * 10);
            versions.add(base);
        }
        
        // Verify all versions are independent
        for (int i = 0; i < versions.size(); i++) {
            PersistentTreeMapSlow<Integer, Integer> version = versions.get(i);
            assertEquals(i + 1, version.size());
            
            for (int j = 0; j <= i; j++) {
                assertEquals(j * 10, version.get(j));
            }
        }
    }

    @Test
    void testToString() {
        assertEquals("{}", emptyMap.toString());

        PersistentTreeMapSlow<String, Integer> simple =
                new PersistentTreeMapSlow<>();
        simple = simple.putInternal("b", 2);
        simple = simple.putInternal("a", 1);
        simple = simple.putInternal("c", 3);

        String str = simple.toString();
        // Should be in sorted order: a=1, b=2, c=3
        assertTrue(str.contains("a=1"));
        assertTrue(str.contains("b=2"));
        assertTrue(str.contains("c=3"));
        assertTrue(str.startsWith("{") && str.endsWith("}"));
    }

    @Test
    void testEqualsAndHashCode() {
        PersistentTreeMapSlow<String, Integer> map1 =
                new PersistentTreeMapSlow<>();
        map1 = map1.putInternal("a", 1);
        map1 = map1.putInternal("b", 2);
        map1 = map1.putInternal("c", 3);

        PersistentTreeMapSlow<String, Integer> map2 =
                new PersistentTreeMapSlow<>();
        map2 = map2.putInternal("c", 3);
        map2 = map2.putInternal("a", 1);
        map2 = map2.putInternal("b", 2);

        // Different insertion order, same elements - should be equal
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        // Different size - not equal
        PersistentTreeMapSlow<String, Integer> map3 =
                new PersistentTreeMapSlow<>();
        map3 = map3.putInternal("a", 1);
        map3 = map3.putInternal("b", 2);

        assertNotEquals(map1, map3);

        // Different values - not equal
        PersistentTreeMapSlow<String, Integer> map4 =
                new PersistentTreeMapSlow<>();
        map4 = map4.putInternal("a", 1);
        map4 = map4.putInternal("b", 2);
        map4 = map4.putInternal("c", 4); // Different value

        assertNotEquals(map1, map4);

        // Empty maps should be equal
        PersistentTreeMapSlow<String, Integer> empty1 =
                new PersistentTreeMapSlow<>();
        PersistentTreeMapSlow<String, Integer> empty2 =
                new PersistentTreeMapSlow<>();
        assertEquals(empty1, empty2);
    }

    @Test
    void testCompareToStandardMap() {
        Map<String, Integer> standardMap = new java.util.TreeMap<>();
        standardMap.put("five", 5);
        standardMap.put("four", 4);
        standardMap.put("one", 1);
        standardMap.put("three", 3);
        standardMap.put("two", 2);

        assertEquals(standardMap.size(), map.size());
        assertEquals(standardMap, map);
        assertEquals(map, standardMap);
    }

    @Test
    void testStreamSupport() {
        // Используем entrySet().stream()
        List<String> keys = map.entrySet().stream().map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Should be in sorted order
        assertEquals(
                Arrays.asList("five", "four", "one", "three", "two"),
                keys
        );

        // Filter by value
        List<String> filteredKeys =
                map.entrySet().stream()
                        .filter(entry -> entry.getValue() > 2)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        assertEquals(Arrays.asList("five", "four", "three"), filteredKeys);
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
    }

    @Test
    void testSingleElementMap() {
        PersistentTreeMapSlow<String, Integer> single = new PersistentTreeMapSlow<>();
        single = single.putInternal("key", 42);
        
        assertEquals(1, single.size());
        assertEquals(42, single.get("key"));
        assertTrue(single.containsKey("key"));
        assertTrue(single.containsValue(42));
        assertEquals("{key=42}", single.toString());
        
        PersistentTreeMapSlow<String, Integer> empty = single.removeInternal("key");
        assertTrue(empty.isEmpty());
    }

    @Test
    void testClassCastExceptionHandling() {
        // These should not throw ClassCastException
        assertNull(map.get(123)); // Wrong type for key
        assertFalse(map.containsKey(123));
        assertFalse(map.containsValue("wrong type"));
    }

    @Test
    void testDuplicateKeys() {
        PersistentTreeMapSlow<String, Integer> map1 = new PersistentTreeMapSlow<>();
        map1 = map1.putInternal("key", 1);
        map1 = map1.putInternal("key", 2); // Update
        
        assertEquals(1, map1.size());
        assertEquals(2, map1.get("key"));
    }

    @Test
    void testEntrySetOperations() {
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        
        // Test equals
        Set<Map.Entry<String, Integer>> expectedSet = new HashSet<>();
        expectedSet.add(new AbstractMap.SimpleEntry<>("five", 5));
        expectedSet.add(new AbstractMap.SimpleEntry<>("four", 4));
        expectedSet.add(new AbstractMap.SimpleEntry<>("one", 1));
        expectedSet.add(new AbstractMap.SimpleEntry<>("three", 3));
        expectedSet.add(new AbstractMap.SimpleEntry<>("two", 2));
        
        assertEquals(expectedSet, entrySet);
        assertEquals(entrySet.hashCode(), expectedSet.hashCode());
    }

    // ========== Helper Methods ==========

    private void assertNotNull(Object obj) {
        assertTrue(obj != null);
    }
}
