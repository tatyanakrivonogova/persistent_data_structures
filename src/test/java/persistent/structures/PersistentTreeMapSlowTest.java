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
@SuppressWarnings({"MagicNumber", "LineLength"})
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
        temp = temp.put("one", 1);
        temp = temp.put("two", 2);
        temp = temp.put("three", 3);
        temp = temp.put("four", 4);
        temp = temp.put("five", 5);
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
        PersistentTreeMapSlow<String, Integer> newMap = map.put("six", 6);

        assertEquals(6, newMap.size());
        assertEquals(6, newMap.get("six"));
        assertTrue(newMap.containsKey("six"));

        // Original map unchanged
        assertEquals(5, map.size());
        assertFalse(map.containsKey("six"));
    }

    @Test
    void testPutExistingKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.put("three", 33);

        assertEquals(5, newMap.size());
        assertEquals(33, newMap.get("three"));

        // Original map unchanged
        assertEquals(3, map.get("three"));
    }

    @Test
    void testPutNullKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.put(null, 99);

        // Should ignore null key
        assertEquals(5, newMap.size());
        assertNull(newMap.get(null));
    }

    @Test
    void testRemove() {
        PersistentTreeMapSlow<String, Integer> newMap = map.remove("three");

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
    void testRemoveNonExistingKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.remove("six");

        // Should return same instance
        assertSame(map, newMap);
        assertEquals(5, newMap.size());
    }

    @Test
    void testRemoveNullKey() {
        PersistentTreeMapSlow<String, Integer> newMap = map.remove(null);

        // Should return same instance
        assertSame(map, newMap);
        assertEquals(5, newMap.size());
    }

    // ========== Collection Interface Tests ==========

    @Test
    void testIterator() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map) {
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
        Iterator<Map.Entry<String, Integer>> iterator = emptyMap.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorRemoveThrows() {
        Iterator<Map.Entry<String, Integer>> iterator = map.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testToArray() {
        Object[] array = map.toArray();
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
        Map.Entry<String, Integer>[] array = map.toArray(new Map.Entry[0]);

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
        assertTrue(map.contains(new AbstractMap.SimpleEntry<>("one", 1)));
        assertTrue(map.contains(new AbstractMap.SimpleEntry<>("two", 2)));
        assertTrue(map.contains(new AbstractMap.SimpleEntry<>("three", 3)));
        // Wrong value
        assertFalse(map.contains(new AbstractMap.SimpleEntry<>("one", 2)));
        // Wrong key
        assertFalse(map.contains(new AbstractMap.SimpleEntry<>("six", 6)));
        assertFalse(map.contains("not an entry"));
    }

    @Test
    void testContainsAll() {
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("two", 2),
                        new AbstractMap.SimpleEntry<>("three", 3));

        assertTrue(map.containsAll(entries));

        Collection<Map.Entry<String, Integer>> mixedEntries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("six", 6) // Doesn't exist
                );

        assertFalse(map.containsAll(mixedEntries));

        // Empty collection should always return true
        assertTrue(map.containsAll(Collections.emptyList()));
    }

    @Test
    void testAddThrows() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("six", 6);
        assertThrows(UnsupportedOperationException.class, () -> map.add(entry));
    }

    @Test
    void testRemoveEntryThrows() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("one", 1);
        assertThrows(UnsupportedOperationException.class, () -> map.remove(entry));
    }

    @Test
    void testAddAllThrows() {
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("six", 6),
                        new AbstractMap.SimpleEntry<>("seven", 7));

        assertThrows(UnsupportedOperationException.class,
                () -> map.addAll(entries));
    }

    @Test
    void testRemoveAllThrows() {
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 1),
                        new AbstractMap.SimpleEntry<>("two", 2));

        assertThrows(UnsupportedOperationException.class,
                () -> map.removeAll(entries));
    }

    @Test
    void testRetainAllThrows() {
        Collection<Map.Entry<String, Integer>> entries =
                Arrays.asList(
                        new AbstractMap.SimpleEntry<>("one", 3),
                        new AbstractMap.SimpleEntry<>("two", 4));

        assertThrows(UnsupportedOperationException.class,
                () -> map.retainAll(entries));
    }

    @Test
    void testClearThrows() {
        assertThrows(UnsupportedOperationException.class, map::clear);
    }

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
    }

    // ========== Persistence Tests ==========

    @Test
    void testImmutability() {
        // Create original map
        PersistentTreeMapSlow<String, Integer> original = new PersistentTreeMapSlow<>();
        original = original.put("a", 1);
        original = original.put("b", 2);

        // Get snapshot
        PersistentTreeMapSlow<String, Integer> snapshot =
                (PersistentTreeMapSlow<String, Integer>) original.snapshot();

        // Create new version (original remains unchanged)
        PersistentTreeMapSlow<String, Integer> modified = original.put("c", 3);

        // Snapshot unchanged
        assertEquals(2, snapshot.size());
        assertTrue(snapshot.containsKey("a"));
        assertTrue(snapshot.containsKey("b"));
        assertFalse(snapshot.containsKey("c"));

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

        PersistentTreeMapSlow<String, Integer> v2 = v1.put("a", 1);
        PersistentTreeMapSlow<String, Integer> v3 = v2.put("b", 2);
        PersistentTreeMapSlow<String, Integer> v4 = v3.put("c", 3);

        // Remove from middle version
        PersistentTreeMapSlow<String, Integer> v5 = v3.remove("b");

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
            large = large.put(i, "Value" + i);
        }

        assertEquals(count, large.size());

        // Verify all entries are present
        for (int i = 0; i < count; i++) {
            assertEquals("Value" + i, large.get(i));
            assertTrue(large.containsKey(i));
        }
    }

    @Test
    void testToString() {
        assertEquals("{}", emptyMap.toString());

        PersistentTreeMapSlow<String, Integer> simple =
                new PersistentTreeMapSlow<>();
        simple = simple.put("b", 2);
        simple = simple.put("a", 1);
        simple = simple.put("c", 3);

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
        map1 = map1.put("a", 1);
        map1 = map1.put("b", 2);
        map1 = map1.put("c", 3);

        PersistentTreeMapSlow<String, Integer> map2 =
                new PersistentTreeMapSlow<>();
        map2 = map2.put("c", 3);
        map2 = map2.put("a", 1);
        map2 = map2.put("b", 2);

        // Different insertion order, same elements - should be equal
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        // Different size - not equal
        PersistentTreeMapSlow<String, Integer> map3 =
                new PersistentTreeMapSlow<>();
        map3 = map3.put("a", 1);
        map3 = map3.put("b", 2);

        assertNotEquals(map1, map3);

        // Different values - not equal
        PersistentTreeMapSlow<String, Integer> map4 =
                new PersistentTreeMapSlow<>();
        map4 = map4.put("a", 1);
        map4 = map4.put("b", 2);
        map4 = map4.put("c", 4); // Different value

        assertNotEquals(map1, map4);

        // Empty maps should be equal
        PersistentTreeMapSlow<String, Integer> empty1 =
                new PersistentTreeMapSlow<>();
        PersistentTreeMapSlow<String, Integer> empty2 =
                new PersistentTreeMapSlow<>();
        assertEquals(empty1, empty2);
    }

    @Test
    void testStreamSupport() {
        List<String> keys = map.stream().map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Should be in sorted order
        assertEquals(
                Arrays.asList("five", "four", "one", "three", "two"),
                keys
        );

        // Filter by value
        List<String> filteredKeys =
                map.stream()
                        .filter(entry -> entry.getValue() > 2)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        assertEquals(Arrays.asList("five", "four", "three"), filteredKeys);
    }
}

