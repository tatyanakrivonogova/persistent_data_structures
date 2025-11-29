package persistent.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PersistentDoublyLinkedListTest implementation.
 */
public class PersistentDoublyLinkedListTest {

    @Test
    @DisplayName("Should create an empty list")
    void testEmptyList() {
        var list = new PersistentDoublyLinkedList<Integer>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertEquals("[]", list.toString());
    }

    @Test
    @DisplayName("Should add last elements correctly")
    void testAddLast() {
        var a = new PersistentDoublyLinkedList<Integer>();
        var b = a.addLast(1);
        var c = b.addLast(2);

        assertEquals("[1]", b.toString());
        assertEquals("[1, 2]", c.toString());

        // old versions are the same
        assertEquals("[]", a.toString());
        assertEquals("[1]", b.toString());
    }

    @Test
    @DisplayName("Should add first elements correctly")
    void testAddFirst() {
        var a = new PersistentDoublyLinkedList<Integer>();
        var b = a.addFirst(1);
        var c = b.addFirst(0);

        // old versions re the same
        assertEquals("[1]", b.toString());
        assertEquals("[0, 1]", c.toString());
    }

    @Test
    @DisplayName("Should add elements by index correctly")
    void testAddByIndex() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(1).addLast(3);

        var list2 = list.add(1, 2);

        assertEquals("[1, 3]", list.toString());
        assertEquals("[1, 2, 3]", list2.toString());
    }

    @Test
    @DisplayName("Should remove elements by index correctly")
    void testRemoveByIndex() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(1).addLast(2).addLast(3);

        var list2 = list.remove(1); // remove 2

        assertEquals("[1, 2, 3]", list.toString());
        assertEquals("[1, 3]", list2.toString());
    }

    @Test
    @DisplayName("Should remove the first and the last elements correctly")
    void testRemoveFirstLast() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(1).addLast(2).addLast(3);

        var a = list.removeFirst();
        var b = list.removeLast();

        assertEquals("[1, 2, 3]", list.toString());
        assertEquals("[2, 3]", a.toString());
        assertEquals("[1, 2]", b.toString());
    }

    @Test
    @DisplayName("get() should work correctly")
    void testGet() {
        var list = new PersistentDoublyLinkedList<String>()
                .addLast("a").addLast("b").addLast("c");

        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException on invalid args")
    void testIndexExceptions() {
        var list = new PersistentDoublyLinkedList<Integer>().addLast(1);

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(5, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
    }

    @Test
    @DisplayName(
            "Should throw IndexOutOfBoundsException " +
                    "on removing from an empty list"
    )
    void testRemoveFromEmpty() {
        var empty = new PersistentDoublyLinkedList<Integer>();

        assertThrows(NoSuchElementException.class, empty::removeFirst);
        assertThrows(NoSuchElementException.class, empty::removeLast);
    }

    @Test
    @DisplayName("Should leave old versions without changes")
    void testPersistence() {
        var a = new PersistentDoublyLinkedList<Integer>();
        var b = a.addLast(1);
        var c = b.addLast(2);
        var d = c.remove(0);

        assertEquals("[]", a.toString());
        assertEquals("[1]", b.toString());
        assertEquals("[1, 2]", c.toString());
        assertEquals("[2]", d.toString());
    }

    @Test
    @DisplayName("Should return valid iterator")
    void testIterator() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(10).addLast(20).addLast(30);

        int[] expected = {10, 20, 30};
        int i = 0;
        for (int v : list) {
            assertEquals(expected[i++], v);
        }

        assertEquals(3, i);
    }
}

