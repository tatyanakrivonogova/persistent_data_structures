package persistent.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Unit tests for PersistentBinaryTree implementation.
 */
class PersistentBinaryTreeTest {

    @Test
    void testEmptyTree() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<>();
        
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(0, tree.height());
        assertTrue(tree.isBalanced());
        assertFalse(tree.contains(5));
    }

    @Test
    void testInsertSingleElement() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<>();
        PersistentBinaryTree<Integer> newTree = tree.insert(10);
        
        assertNotSame(tree, newTree);
        assertTrue(tree.isEmpty());
        assertFalse(newTree.isEmpty());
        assertEquals(1, newTree.size());
        assertEquals(1, newTree.height());
        assertTrue(newTree.contains(10));
        assertTrue(newTree.isBalanced());
    }

    @Test
    void testInsertMultipleElements() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7)
            .insert(1)
            .insert(4);
        
        assertEquals(5, tree.size());
        assertTrue(tree.contains(5));
        assertTrue(tree.contains(3));
        assertTrue(tree.contains(7));
        assertTrue(tree.contains(1));
        assertTrue(tree.contains(4));
        assertFalse(tree.contains(10));
        assertTrue(tree.isBalanced());
    }

    @Test
    void testRemoveElement() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7);
        
        PersistentBinaryTree<Integer> withoutThree = tree.remove(3);
        
        assertEquals(3, tree.size());
        assertEquals(2, withoutThree.size());
        assertTrue(tree.contains(3));
        assertFalse(withoutThree.contains(3));
        assertTrue(withoutThree.contains(5));
        assertTrue(withoutThree.contains(7));
        assertTrue(withoutThree.isBalanced());
    }

    @Test
    void testMinAndMax() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7)
            .insert(1)
            .insert(9);
        
        assertEquals(Integer.valueOf(1), tree.min());
        assertEquals(Integer.valueOf(9), tree.max());
    }

    @Test
    void testMinOnEmptyTree() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<>();
        
        assertThrows(NoSuchElementException.class, tree::min);
    }

    @Test
    void testMaxOnEmptyTree() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<>();
        
        assertThrows(NoSuchElementException.class, tree::max);
    }

    @Test
    void testTraversals() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(4)
            .insert(2)
            .insert(6)
            .insert(1)
            .insert(3)
            .insert(5)
            .insert(7);
        
        // Test in-order traversal (should be sorted)
        List<Integer> inOrder = new ArrayList<>();
        tree.inOrderTraversal(inOrder::add);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), inOrder);
        
        // Test pre-order traversal
        List<Integer> preOrder = new ArrayList<>();
        tree.preOrderTraversal(preOrder::add);
        assertEquals(Integer.valueOf(4), preOrder.get(0)); // Root first
        
        // Test post-order traversal
        List<Integer> postOrder = new ArrayList<>();
        tree.postOrderTraversal(postOrder::add);
        assertEquals(Integer.valueOf(4), postOrder.get(postOrder.size() - 1)); // Root last
    }

    @Test
    void testBalancing() {
        // This should create a balanced tree even with sorted input
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(1)
            .insert(2)
            .insert(3)
            .insert(4)
            .insert(5);
        
        assertTrue(tree.isBalanced());
        assertTrue(tree.height() <= 3); // For 5 elements, height should be 3 or less in balanced tree
    }

    @Test
    void testIterator() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(3)
            .insert(1)
            .insert(4)
            .insert(2);
        
        List<Integer> result = new ArrayList<>();
        for (Integer value : tree) {
            result.add(value);
        }
        
        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    void testPersistence() {
        PersistentBinaryTree<Integer> original = new PersistentBinaryTree<Integer>()
            .insert(1)
            .insert(2)
            .insert(3);
        
        PersistentBinaryTree<Integer> modified = original.insert(4);
        
        // Original should remain unchanged
        assertEquals(3, original.size());
        assertFalse(original.contains(4));
        
        // Modified should have new element
        assertEquals(4, modified.size());
        assertTrue(modified.contains(4));
        
        // Both should be balanced
        assertTrue(original.isBalanced());
        assertTrue(modified.isBalanced());
    }

    @Test
    void testComplexOperations() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(50)
            .insert(30)
            .insert(70)
            .insert(20)
            .insert(40)
            .insert(60)
            .insert(80);
        
        // Test multiple operations
        PersistentBinaryTree<Integer> result = tree
            .remove(30)
            .insert(35)
            .remove(70)
            .insert(75);
        
        assertEquals(7, tree.size());
        assertEquals(7, result.size());
        assertFalse(result.contains(30));
        assertTrue(result.contains(35));
        assertFalse(result.contains(70));
        assertTrue(result.contains(75));
        assertTrue(result.isBalanced());
        
        // Verify order is maintained
        List<Integer> sorted = toList(result);
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1) < sorted.get(i));
        }
    }

    @Test
    void testVersioning() {
        PersistentBinaryTree<Integer> v1 = new PersistentBinaryTree<>();
        PersistentBinaryTree<Integer> v2 = v1.insert(1);
        PersistentBinaryTree<Integer> v3 = v2.insert(2);
        
        assertNotEquals(v1.getVersion(), v2.getVersion());
        assertNotEquals(v2.getVersion(), v3.getVersion());
    }

    @Test
    void testStringTree() {
        // Test with String type (also Comparable)
        PersistentBinaryTree<String> tree = new PersistentBinaryTree<String>()
            .insert("banana")
            .insert("apple")
            .insert("cherry");
        
        assertEquals(3, tree.size());
        assertEquals("apple", tree.min());
        assertEquals("cherry", tree.max());
        assertTrue(tree.contains("banana"));
        assertFalse(tree.contains("date"));
    }

    @Test
    void testComplexBalancingScenario() {
        // Test a scenario that requires multiple rotations
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(10)
            .insert(20)
            .insert(30)  // This should trigger left rotation
            .insert(5)
            .insert(15)
            .insert(25)
            .insert(35);
        
        assertTrue(tree.isBalanced());
        assertEquals(7, tree.size());
        
        // All elements should be present
        assertTrue(tree.contains(10));
        assertTrue(tree.contains(20));
        assertTrue(tree.contains(30));
        assertTrue(tree.contains(5));
        assertTrue(tree.contains(15));
        assertTrue(tree.contains(25));
        assertTrue(tree.contains(35));
    }

    @Test
    void testRemoveRoot() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7);
        
        PersistentBinaryTree<Integer> withoutRoot = tree.remove(5);
        
        assertEquals(3, tree.size());
        assertEquals(2, withoutRoot.size());
        assertTrue(tree.contains(5));
        assertFalse(withoutRoot.contains(5));
        assertTrue(withoutRoot.contains(3));
        assertTrue(withoutRoot.contains(7));
        assertTrue(withoutRoot.isBalanced());
    }

    @Test
    void testRemoveLeaf() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7)
            .insert(1);
        
        PersistentBinaryTree<Integer> withoutLeaf = tree.remove(1);
        
        assertEquals(4, tree.size());
        assertEquals(3, withoutLeaf.size());
        assertTrue(tree.contains(1));
        assertFalse(withoutLeaf.contains(1));
        assertTrue(withoutLeaf.isBalanced());
    }

    @Test
    void testRemoveNodeWithTwoChildren() {
        PersistentBinaryTree<Integer> tree = new PersistentBinaryTree<Integer>()
            .insert(5)
            .insert(3)
            .insert(7)
            .insert(2)
            .insert(4)
            .insert(6)
            .insert(8);
        
        PersistentBinaryTree<Integer> withoutNode = tree.remove(5); // Root with two children
        
        assertEquals(7, tree.size());
        assertEquals(6, withoutNode.size());
        assertTrue(tree.contains(5));
        assertFalse(withoutNode.contains(5));
        assertTrue(withoutNode.isBalanced());
        
        // All other elements should still be present
        assertTrue(withoutNode.contains(3));
        assertTrue(withoutNode.contains(7));
        assertTrue(withoutNode.contains(2));
        assertTrue(withoutNode.contains(4));
        assertTrue(withoutNode.contains(6));
        assertTrue(withoutNode.contains(8));
    }

    @Test
    void testTreeWithCustomComparable() {
        // Test with a custom Comparable class
        class Name implements Comparable<Name> {
            final String firstName;
            final String lastName;
            
            Name(String firstName, String lastName) {
                this.firstName = firstName;
                this.lastName = lastName;
            }
            
            @Override
            public int compareTo(Name other) {
                int lastCompare = lastName.compareTo(other.lastName);
                if (lastCompare != 0) return lastCompare;
                return firstName.compareTo(other.firstName);
            }
            
            @Override
            public String toString() {
                return firstName + " " + lastName;
            }
        }
        
        PersistentBinaryTree<Name> nameTree = new PersistentBinaryTree<Name>()
            .insert(new Name("John", "Doe"))
            .insert(new Name("Jane", "Smith"))
            .insert(new Name("Bob", "Adams"));
        
        assertEquals(3, nameTree.size());
        assertEquals("Bob Adams", nameTree.min().toString());
        assertEquals("Jane Smith", nameTree.max().toString());
    }

    // Helper method to convert tree to list
    private List<Integer> toList(PersistentBinaryTree<Integer> tree) {
        List<Integer> result = new ArrayList<>();
        tree.inOrderTraversal(result::add);
        return result;
    }
}