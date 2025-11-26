package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PersistentTreeMap implementation.
 */
class PersistentTreeMapTest {

    @Test
    void testEmptyMap() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertEquals(0, map.height());
        assertTrue(map.isBalanced());
        assertFalse(map.containsKey("key"));
        assertNull(map.get("key"));
    }

    @Test
    void testPutSingleElement() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();
        PersistentTreeMap<String, Integer> newMap = map.put("one", 1);

        assertNotSame(map, newMap);
        assertTrue(map.isEmpty());
        assertFalse(newMap.isEmpty());
        assertEquals(1, newMap.size());
        assertEquals(1, newMap.height());
        assertTrue(newMap.containsKey("one"));
        assertEquals(Integer.valueOf(1), newMap.get("one"));
        assertTrue(newMap.isBalanced());
    }

    @Test
    void testPutMultipleElements() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3);

        assertEquals(3, map.size());
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));
        assertTrue(map.containsKey("three"));
        assertEquals(Integer.valueOf(1), map.get("one"));
        assertEquals(Integer.valueOf(2), map.get("two"));
        assertEquals(Integer.valueOf(3), map.get("three"));
        assertTrue(map.isBalanced());
    }

    @Test
    void testPutUpdateExisting() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("key", 1);

        PersistentTreeMap<String, Integer> updated = map.put("key", 100);

        assertEquals(1, map.size());
        assertEquals(1, updated.size());
        assertEquals(Integer.valueOf(1), map.get("key"));
        assertEquals(Integer.valueOf(100), updated.get("key"));
        assertTrue(updated.isBalanced());
    }

    @Test
    void testRemoveElement() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3);

        PersistentTreeMap<String, Integer> withoutTwo = map.remove("two");

        assertEquals(3, map.size());
        assertEquals(2, withoutTwo.size());
        assertTrue(map.containsKey("two"));
        assertFalse(withoutTwo.containsKey("two"));
        assertTrue(withoutTwo.containsKey("one"));
        assertTrue(withoutTwo.containsKey("three"));
        assertTrue(withoutTwo.isBalanced());
    }

    @Test
    void testRemoveNonExistentElement() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>().put("key", 1);
        PersistentTreeMap<String, Integer> sameMap = map.remove("nonexistent");

        assertSame(map, sameMap);
        assertEquals(1, sameMap.size());
        assertEquals(map, sameMap);
        assertTrue(sameMap.containsKey("key"));
        assertEquals(Integer.valueOf(1), sameMap.get("key"));
    }

    @Test
    void testRemoveFromEmptyMap() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();
        PersistentTreeMap<String, Integer> sameMap = map.remove("key");

        assertSame(map, sameMap);
        assertTrue(sameMap.isEmpty());
    }

    @Test
    void testFirstAndLastKey() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("banana", 3)
            .put("apple", 1)
            .put("cherry", 2);

        assertEquals("apple", map.firstKey());
        assertEquals("cherry", map.lastKey());
    }

    @Test
    void testFirstKeyOnEmptyMap() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();

        assertThrows(NoSuchElementException.class, map::firstKey);
    }

    @Test
    void testLastKeyOnEmptyMap() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();

        assertThrows(NoSuchElementException.class, map::lastKey);
    }

    @Test
    void testContainsValue() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3);

        assertTrue(map.containsValue(1));
        assertTrue(map.containsValue(2));
        assertTrue(map.containsValue(3));
        assertFalse(map.containsValue(4));
    }

    @Test
    void testIterator() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("c", 3)
            .put("a", 1)
            .put("b", 2);

        List<String> keys = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (Entry<String, Integer> entry : map) {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }

        // Should be in sorted key order
        assertEquals(List.of("a", "b", "c"), keys);
        assertEquals(List.of(1, 2, 3), values);
    }

    @Test
    void testPersistence() {
        PersistentTreeMap<String, Integer> original = new PersistentTreeMap<String, Integer>()
            .put("one", 1)
            .put("two", 2);

        PersistentTreeMap<String, Integer> modified = original.put("three", 3);

        // Original should remain unchanged
        assertEquals(2, original.size());
        assertFalse(original.containsKey("three"));

        // Modified should have new element
        assertEquals(3, modified.size());
        assertTrue(modified.containsKey("three"));

        // Both should be balanced
        assertTrue(original.isBalanced());
        assertTrue(modified.isBalanced());
    }

    @Test
    void testComplexOperations() {
        PersistentTreeMap<Integer, String> map = new PersistentTreeMap<Integer, String>()
            .put(50, "fifty")
            .put(30, "thirty")
            .put(70, "seventy")
            .put(20, "twenty")
            .put(40, "forty")
            .put(60, "sixty")
            .put(80, "eighty");

        // Test multiple operations
        PersistentTreeMap<Integer, String> result = map
            .remove(30)
            .put(35, "thirty-five")
            .remove(70)
            .put(75, "seventy-five");

        assertEquals(7, map.size());
        assertEquals(7, result.size());
        assertFalse(result.containsKey(30));
        assertTrue(result.containsKey(35));
        assertEquals("thirty-five", result.get(35));
        assertFalse(result.containsKey(70));
        assertTrue(result.containsKey(75));
        assertEquals("seventy-five", result.get(75));
        assertTrue(result.isBalanced());
    }

    @Test
    void testBalancingWithSortedInput() {
        // Insert sorted keys to test balancing
        PersistentTreeMap<Integer, String> map = new PersistentTreeMap<>();
        for (int i = 1; i <= 10; i++) {
            map = map.put(i, "value" + i);
        }

        assertTrue(map.isBalanced());
        assertTrue(map.height() <= 4); // For 10 elements, height should be 4 or less in balanced tree
        assertEquals(10, map.size());
    }

    @Test
    void testVersioning() {
        PersistentTreeMap<String, Integer> v1 = new PersistentTreeMap<>();
        PersistentTreeMap<String, Integer> v2 = v1.put("a", 1);
        PersistentTreeMap<String, Integer> v3 = v2.put("b", 2);

        assertNotEquals(v1.getVersion(), v2.getVersion());
        assertNotEquals(v2.getVersion(), v3.getVersion());
    }

    @Test
    void testNullValues() {
        PersistentTreeMap<String, String> map = new PersistentTreeMap<String, String>()
            .put("key1", null)
            .put("key2", "value");

        assertFalse(map.containsKey("key1"));
        assertNull(map.get("key1"));
        assertEquals("value", map.get("key2"));
    }

    @Test
    void testComplexKeyTypes() {
        // Test with complex keys
        class ComplexKey implements Comparable<ComplexKey> {
            final int id;
            final String name;

            ComplexKey(int id, String name) {
                this.id = id;
                this.name = name;
            }

            @Override
            public int compareTo(ComplexKey other) {
                int idCompare = Integer.compare(id, other.id);
                if (idCompare != 0) return idCompare;
                return name.compareTo(other.name);
            }
        }

        PersistentTreeMap<ComplexKey, String> map = new PersistentTreeMap<ComplexKey, String>()
            .put(new ComplexKey(1, "first"), "value1")
            .put(new ComplexKey(2, "second"), "value2")
            .put(new ComplexKey(3, "third"), "value3");

        assertEquals(3, map.size());
        assertTrue(map.containsKey(new ComplexKey(1, "first")));
        assertEquals("value1", map.get(new ComplexKey(1, "first")));
        assertTrue(map.isBalanced());
    }

    @Test
    void testLargeMap() {
        PersistentTreeMap<Integer, Integer> map = new PersistentTreeMap<>();
        for (int i = 0; i < 100; i++) {
            map = map.put(i, i * 10);
        }

        assertEquals(100, map.size());
        assertTrue(map.isBalanced());

        // Test random access
        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i * 10), map.get(i));
        }

        // Test first and last
        assertEquals(Integer.valueOf(0), map.firstKey());
        assertEquals(Integer.valueOf(99), map.lastKey());
    }

    @Test
    void testToString() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<String, Integer>()
            .put("b", 2)
            .put("a", 1)
            .put("c", 3);

        String result = map.toString();
        assertTrue(result.contains("a=1"));
        assertTrue(result.contains("b=2"));
        assertTrue(result.contains("c=3"));
        assertTrue(result.startsWith("PersistentTreeMap{"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testEmptyMapToString() {
        PersistentTreeMap<String, Integer> map = new PersistentTreeMap<>();
        assertEquals("PersistentTreeMap{}", map.toString());
    }
}
