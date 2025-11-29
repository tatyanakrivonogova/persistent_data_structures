package persistent.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PersistentDoublyLinkedListTest {

    /** Const 0. */
    private static final int ZERO = 0;

    /** Const 1. */
    private static final int ONE  = 1;

    /** Const 2. */
    private static final int TWO  = 2;

    /** Const 3. */
    private static final int THREE = 3;

    /** Const 5. */
    private static final int FIVE = 5;

    /** Const 10. */
    private static final int TEN = 10;

    /** Const 20. */
    private static final int TWENTY = 20;

    /** Const 30. */
    private static final int THIRTY = 30;

    @Test
    @DisplayName("Should create an empty list")
    void testEmptyList() {
        var list = new PersistentDoublyLinkedList<Integer>();
        assertTrue(list.isEmpty());
        assertEquals(ZERO, list.size());
        assertEquals("[]", list.toString());
    }

    @Test
    @DisplayName("Should add last elements correctly")
    void testAddLast() {
        var a = new PersistentDoublyLinkedList<Integer>();
        var b = a.addLast(ONE);
        var c = b.addLast(TWO);

        assertEquals("[1]", b.toString());
        assertEquals("[1, 2]", c.toString());

        assertEquals("[]", a.toString());
        assertEquals("[1]", b.toString());
    }

    @Test
    @DisplayName("Should add first elements correctly")
    void testAddFirst() {
        var a = new PersistentDoublyLinkedList<Integer>();
        var b = a.addFirst(ONE);
        var c = b.addFirst(ZERO);

        assertEquals("[1]", b.toString());
        assertEquals("[0, 1]", c.toString());
    }

    @Test
    @DisplayName("Should add elements by index correctly")
    void testAddByIndex() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(ONE).addLast(THREE);

        var list2 = list.add(ONE, TWO);

        assertEquals("[1, 3]", list.toString());
        assertEquals("[1, 2, 3]", list2.toString());
    }

    @Test
    @DisplayName("Should remove elements by index correctly")
    void testRemoveByIndex() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(ONE).addLast(TWO).addLast(THREE);

        var list2 = list.remove(ONE);

        assertEquals("[1, 2, 3]", list.toString());
        assertEquals("[1, 3]", list2.toString());
    }

    @Test
    @DisplayName(
            "Should remove the first and the last elements correctly"
    )
    void testRemoveFirstLast() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(ONE).addLast(TWO).addLast(THREE);

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

        assertEquals("a", list.get(ZERO));
        assertEquals("b", list.get(ONE));
        assertEquals("c", list.get(TWO));
    }

    @Test
    @DisplayName(
            "Should throw IndexOutOfBoundsException on invalid args"
    )
    void testIndexExceptions() {
        var list = new PersistentDoublyLinkedList<Integer>().addLast(ONE);

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-ONE));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(TWO));

        assertThrows(IndexOutOfBoundsException.class,
                () -> list.add(FIVE, TEN)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(ONE));
    }

    @Test
    @DisplayName(
            "Should throw Exception on removing from an empty list"
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
        var b = a.addLast(ONE);
        var c = b.addLast(TWO);
        var d = c.remove(ZERO);

        assertEquals("[]", a.toString());
        assertEquals("[1]", b.toString());
        assertEquals("[1, 2]", c.toString());
        assertEquals("[2]", d.toString());
    }

    @Test
    @DisplayName("Should return valid iterator")
    void testIterator() {
        var list = new PersistentDoublyLinkedList<Integer>()
                .addLast(TEN).addLast(TWENTY).addLast(THIRTY);

        int[] expected = {TEN, TWENTY, THIRTY};
        int i = ZERO;
        for (int v : list) {
            assertEquals(expected[i++], v);
        }

        assertEquals(THREE, i);
    }
}
