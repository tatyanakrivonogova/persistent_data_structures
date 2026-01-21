package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive tests for PersistentDoublyLinkedListFat and
 * TransactionalPersistentDoublyLinkedListFat.
 */
@DisplayName("Persistent Doubly Linked List Fat Tests")
@SuppressWarnings({"MagicNumber", "LineLength", "LongLine", "MaxLineLength"})
class PersistentDoublyLinkedListFatTest {

    /** Empty list for testing. */
    private PersistentDoublyLinkedListFat<Integer> emptyList;
    /** List with elements for testing. */
    private PersistentDoublyLinkedListFat<Integer> listWithElements;

    @BeforeEach
    void setUp() {
        emptyList = new PersistentDoublyLinkedListFat<>();
        listWithElements =
                (PersistentDoublyLinkedListFat<Integer>)
                        emptyList.createWithAdded(1).createWithAdded(2).createWithAdded(3);
    }

    @Test
    @DisplayName("Empty list should be empty")
    void testEmptyList() {
        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());
        assertFalse(emptyList.iterator().hasNext());
    }

    @Test
    @DisplayName("Add elements to list")
    void testAddElements() {
        PersistentDoublyLinkedListFat<Integer> list =
                (PersistentDoublyLinkedListFat<Integer>) emptyList.createWithAdded(10);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertTrue(list.containsElement(10));

        list = (PersistentDoublyLinkedListFat<Integer>) list.createWithAdded(20);
        assertEquals(2, list.size());
        assertTrue(list.containsElement(10));
        assertTrue(list.containsElement(20));
    }

    @Test
    @DisplayName("Remove elements from list")
    void testRemoveElements() {
        PersistentDoublyLinkedListFat<Integer> list =
                (PersistentDoublyLinkedListFat<Integer>)
                        listWithElements.createWithRemoved(2);
        assertEquals(2, list.size());
        assertTrue(list.containsElement(1));
        assertFalse(list.containsElement(2));
        assertTrue(list.containsElement(3));

        list = (PersistentDoublyLinkedListFat<Integer>) list.createWithRemoved(1);
        assertEquals(1, list.size());
        assertFalse(list.containsElement(1));
        assertTrue(list.containsElement(3));

        list = (PersistentDoublyLinkedListFat<Integer>) list.createWithRemoved(3);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Remove non-existent element should return same list")
    void testRemoveNonExistent() {
        PersistentDoublyLinkedListFat<Integer> original = listWithElements;
        PersistentDoublyLinkedListFat<Integer> modified =
                (PersistentDoublyLinkedListFat<Integer>) original.createWithRemoved(99);
        assertSame(original, modified);
    }

    @Test
    @DisplayName("Create empty list")
    void testCreateEmpty() {
        PersistentDoublyLinkedListFat<Integer> empty =
                (PersistentDoublyLinkedListFat<Integer>) listWithElements.createEmpty();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
    }

    @Test
    @DisplayName("Check contains element")
    void testContainsElement() {
        assertTrue(listWithElements.containsElement(1));
        assertTrue(listWithElements.containsElement(2));
        assertTrue(listWithElements.containsElement(3));
        assertFalse(listWithElements.containsElement(4));
    }

    @Test
    @DisplayName("Collection contains method")
    void testCollectionContains() {
        assertTrue(listWithElements.contains(1));
        assertTrue(listWithElements.contains(2));
        assertTrue(listWithElements.contains(3));
        assertFalse(listWithElements.contains(4));
        assertFalse(listWithElements.contains("string")); // Wrong type
    }

    @Test
    @DisplayName("Iterator works correctly")
    void testIterator() {
        List<Integer> collected = new ArrayList<>();
        for (Integer element : listWithElements) {
            collected.add(element);
        }

        assertEquals(3, collected.size());
        assertEquals(List.of(1, 2, 3), collected);
    }

    @Test
    @DisplayName("Iterator should not support remove")
    void testIteratorRemove() {
        Iterator<Integer> iterator = listWithElements.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    @DisplayName("To array methods")
    void testToArray() {
        Object[] array = listWithElements.toArray();
        assertArrayEquals(new Object[] {1, 2, 3}, array);

        Integer[] typedArray = listWithElements.toArray(new Integer[0]);
        assertArrayEquals(new Integer[] {1, 2, 3}, typedArray);

        Integer[] largerArray = new Integer[5];
        Integer[] result = listWithElements.toArray(largerArray);
        assertSame(largerArray, result);
        assertArrayEquals(new Integer[] {1, 2, 3, null, null}, result);
    }

    @Test
    @DisplayName("Contains all elements")
    void testContainsAll() {
        List<Integer> checkList = Arrays.asList(1, 3);
        assertTrue(listWithElements.containsAll(checkList));

        List<Integer> notAllList = Arrays.asList(1, 4);
        assertFalse(listWithElements.containsAll(notAllList));
    }

    @Test
    @DisplayName("Immutable operations throw exceptions")
    void testImmutableOperations() {
        PersistentDoublyLinkedListFat<Integer> list = listWithElements;

        // Проверяем, что методы из Collection бросают исключения
        assertThrows(UnsupportedOperationException.class, () -> list.add(4));
        assertThrows(UnsupportedOperationException.class,
                () -> list.remove((Object) 2));
        assertThrows(UnsupportedOperationException.class,
                () -> list.addAll(Arrays.asList(4, 5)));
        assertThrows(UnsupportedOperationException.class,
                () -> list.addAll(0, Arrays.asList(4, 5)));
        assertThrows(UnsupportedOperationException.class,
                () -> list.removeAll(Arrays.asList(1, 2)));
        assertThrows(UnsupportedOperationException.class,
                () -> list.retainAll(Arrays.asList(1)));
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
        assertThrows(UnsupportedOperationException.class, () -> list.set(0, 10));
        assertThrows(UnsupportedOperationException.class, () -> list.add(0, 10));
        assertThrows(UnsupportedOperationException.class, () -> list.addFirst(10));
        assertThrows(UnsupportedOperationException.class, () -> list.addLast(10));
        assertThrows(UnsupportedOperationException.class, () -> list.removeFirst());
        assertThrows(UnsupportedOperationException.class, () -> list.removeLast());

        // Проверяем, что итератор не поддерживает remove
        Iterator<Integer> iterator = list.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);

        // Проверяем ListIterator
        ListIterator<Integer> listIterator = list.listIterator();
        listIterator.next();
        assertThrows(UnsupportedOperationException.class, listIterator::remove);
        assertThrows(UnsupportedOperationException.class, () -> listIterator.set(10));
        assertThrows(UnsupportedOperationException.class, () -> listIterator.add(10));
    }

    @Test
    @DisplayName("Equals and hashCode")
    void testEqualsAndHashCode() {
        PersistentDoublyLinkedListFat<Integer> sameElements =
                (PersistentDoublyLinkedListFat<Integer>)
                        emptyList.createWithAdded(1).createWithAdded(2).createWithAdded(3);

        PersistentDoublyLinkedListFat<Integer> differentElements =
                (PersistentDoublyLinkedListFat<Integer>) emptyList.createWithAdded(4)
                        .createWithAdded(5);

        assertEquals(listWithElements, sameElements);
        assertNotEquals(listWithElements, differentElements);
        assertEquals(listWithElements.hashCode(), sameElements.hashCode());

        // Different type collection with same elements
        List<Integer> arrayList = Arrays.asList(1, 2, 3);
        assertEquals(listWithElements, arrayList);

        // Self equality
        assertEquals(listWithElements, listWithElements);

        // Null comparison
        assertNotEquals(null, listWithElements);

        // Different size
        PersistentDoublyLinkedListFat<Integer> smaller =
                (PersistentDoublyLinkedListFat<Integer>) emptyList.createWithAdded(1);
        assertNotEquals(listWithElements, smaller);
    }

    @Test
    @DisplayName("Get version")
    void testGetVersion() {
        assertNotNull(listWithElements.getVersion());
        assertNotNull(emptyList.getVersion());
    }

    @Test
    @DisplayName("List-specific methods: addFirstInternal, addLastInternal, addInternal at index")
    void testListSpecificAddMethods() {
        PersistentDoublyLinkedListFat<Integer> list = emptyList;

        list = list.addFirstInternal(3); // [3]
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(3), list.get(0));

        list = list.addFirstInternal(1); // [1, 3]
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));

        list = list.addInternal(1, 2); // [1, 2, 3]
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));

        list = list.addLastInternal(4); // [1, 2, 3, 4]
        assertEquals(4, list.size());
        assertEquals(Integer.valueOf(4), list.get(3));

        list = list.addInternal(0, 0); // [0, 1, 2, 3, 4]
        assertEquals(5, list.size());
        assertEquals(Integer.valueOf(0), list.get(0));
    }

    @Test
    @DisplayName("List methods: removeFirstInternal, removeLastInternal, removeInternal at index")
    void testListSpecificRemoveMethods() {
        PersistentDoublyLinkedListFat<Integer> list =
                emptyList.addFirstInternal(1).addLastInternal(2).addLastInternal(3).addLastInternal(4); // [1, 2, 3, 4]

        list = list.removeFirstInternal(); // [2, 3, 4]
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        assertFalse(list.containsElement(1));

        list = list.removeLastInternal(); // [2, 3]
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(3), list.get(1));
        assertFalse(list.containsElement(4));

        list = list.removeInternal(0); // [3]
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(3), list.get(0));

        list = list.removeInternal(0); // []
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Add at invalid index throws exception")
    void testAddInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.addInternal(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.addInternal(1, 1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.addInternal(-1, 1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.addInternal(4, 1));
    }

    @Test
    @DisplayName("Get at invalid index throws exception")
    void testGetInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.get(-1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.get(-1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.get(3));
    }

    @Test
    @DisplayName("Remove at invalid index throws exception")
    void testRemoveInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.removeInternal(0));
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.removeInternal(-1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.removeInternal(-1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> listWithElements.removeInternal(3));
    }

    @Test
    @DisplayName("Remove from empty list throws exception")
    void testRemoveFromEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.removeFirstInternal());
        assertThrows(IndexOutOfBoundsException.class, () -> emptyList.removeLastInternal());
    }

    @Test
    @DisplayName("ToString method")
    void testToString() {
        assertEquals("[]", emptyList.toString());
        assertEquals("[1, 2, 3]", listWithElements.toString());

        PersistentDoublyLinkedListFat<String> stringList =
                (PersistentDoublyLinkedListFat<String>)
                        new PersistentDoublyLinkedListFat<String>().createWithAdded("a")
                                .createWithAdded("b");
        assertEquals("[a, b]", stringList.toString());
    }

    @Test
    @DisplayName("Persistence: original list unchanged after modifications")
    void testPersistence() {
        PersistentDoublyLinkedListFat<Integer> original = listWithElements;
        PersistentDoublyLinkedListFat<Integer> modified =
                (PersistentDoublyLinkedListFat<Integer>) original.createWithAdded(4);

        // Original unchanged
        assertEquals(3, original.size());
        assertFalse(original.containsElement(4));

        // Modified has new element
        assertEquals(4, modified.size());
        assertTrue(modified.containsElement(4));

        // They are different objects
        assertNotSame(original, modified);
    }

    @Test
    @DisplayName("Complex sequence of operations")
    void testComplexOperations() {
        PersistentDoublyLinkedListFat<Integer> list = emptyList;

        // Build list: [5, 1, 3, 2, 4]
        list = list.addFirstInternal(3);
        list = list.addFirstInternal(1);
        list = list.addLastInternal(4);
        list = list.addInternal(1, 2);
        list = list.addFirstInternal(5);

        assertEquals(5, list.size());
        assertEquals(Integer.valueOf(5), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertEquals(Integer.valueOf(2), list.get(2));
        assertEquals(Integer.valueOf(3), list.get(3));
        assertEquals(Integer.valueOf(4), list.get(4));

        // Remove elements: [1, 2, 4]
        list = list.removeFirstInternal(); // Remove 5: [1, 2, 3, 4]
        list = list.removeInternal(2); // Remove 3: [1, 2, 4]

        assertEquals(3, list.size());
        assertFalse(list.containsElement(5));
        assertFalse(list.containsElement(3));
        assertTrue(list.containsElement(1));
        assertTrue(list.containsElement(2));
        assertTrue(list.containsElement(4));

        // Convert to array
        Integer[] array = list.toArray(new Integer[0]);
        assertArrayEquals(new Integer[] {1, 2, 4}, array);
    }

    @Test
    @DisplayName("Test indexOf and lastIndexOf")
    void testIndexOf() {
        PersistentDoublyLinkedListFat<Integer> list =
            emptyList.addFirstInternal(1).addLastInternal(2).addLastInternal(3).addLastInternal(2).addLastInternal(1);

        assertEquals(0, list.indexOf(1));
        assertEquals(1, list.indexOf(2));
        assertEquals(2, list.indexOf(3));
        assertEquals(-1, list.indexOf(4));

        assertEquals(4, list.lastIndexOf(1));
        assertEquals(3, list.lastIndexOf(2));
        assertEquals(2, list.lastIndexOf(3));
        assertEquals(-1, list.lastIndexOf(4));
    }

    @Test
    @DisplayName("Test ListIterator")
    void testListIterator() {
        ListIterator<Integer> iterator = listWithElements.listIterator();

        assertTrue(iterator.hasNext());
        assertFalse(iterator.hasPrevious());
        assertEquals(0, iterator.nextIndex());
        assertEquals(-1, iterator.previousIndex());

        assertEquals(Integer.valueOf(1), iterator.next());
        assertEquals(1, iterator.nextIndex());
        assertEquals(0, iterator.previousIndex());
        assertTrue(iterator.hasPrevious());

        assertEquals(Integer.valueOf(2), iterator.next());
        assertEquals(Integer.valueOf(3), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("Test ListIterator with index")
    void testListIteratorWithIndex() {
        ListIterator<Integer> iterator = listWithElements.listIterator(1);

        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.nextIndex());
        assertEquals(0, iterator.previousIndex());

        assertEquals(Integer.valueOf(2), iterator.next());
        assertEquals(Integer.valueOf(3), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("Test subList")
    void testSubList() {
        PersistentDoublyLinkedListFat<Integer> list = 
            emptyList.addFirstInternal(1).addLastInternal(2).addLastInternal(3).addLastInternal(4).addLastInternal(5);

        List<Integer> subList = list.subList(1, 4);
        assertEquals(3, subList.size());
        assertEquals(List.of(2, 3, 4), subList);

        // Empty sublist
        List<Integer> emptySubList = list.subList(2, 2);
        assertTrue(emptySubList.isEmpty());
    }

    @Test
    @DisplayName("Test subList invalid indices")
    void testSubListInvalidIndices() {
        PersistentDoublyLinkedListFat<Integer> list =
            emptyList.addFirstInternal(1).addLastInternal(2).addLastInternal(3);

        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(-1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(2, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 100})
    @DisplayName("Add multiple elements and iterate")
    void testAddMultipleElements(final int count) {
        PersistentDoublyLinkedListFat<Integer> list = emptyList;
        List<Integer> expected = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            list = (PersistentDoublyLinkedListFat<Integer>) list.createWithAdded(i);
            expected.add(i);
        }

        assertEquals(count, list.size());

        List<Integer> actual = new ArrayList<>();
        for (Integer element : list) {
            actual.add(element);
        }

        assertEquals(expected, actual);
    }
}
