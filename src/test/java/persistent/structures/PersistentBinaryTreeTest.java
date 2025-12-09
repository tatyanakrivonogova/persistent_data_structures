package persistent.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive test suite for PersistentBinaryTree and TransactionalPersistentBinaryTree.
 */
class PersistentBinaryTreeTest {
    
    private PersistentBinaryTree<Integer> emptyTree;
    private PersistentBinaryTree<Integer> tree;
    private TransactionalPersistentBinaryTree<Integer> transactionalTree;
    
    @BeforeEach
    void setUp() {
        emptyTree = new PersistentBinaryTree<>();
        
        // Build tree using createWithAdded
        PersistentBinaryTree<Integer> temp = new PersistentBinaryTree<>();
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(5);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(3);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(7);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(1);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(4);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(6);
        temp = (PersistentBinaryTree<Integer>) temp.createWithAdded(9);
        tree = temp;
        
        // Build transactional tree using add()
        transactionalTree = new TransactionalPersistentBinaryTree<>();
        transactionalTree.add(5);
        transactionalTree.add(3);
        transactionalTree.add(7);
        transactionalTree.add(1);
        transactionalTree.add(4);
        transactionalTree.add(6);
        transactionalTree.add(9);
    }
    
    // ========== Tests for PersistentBinaryTree (immutable) ==========
    
    @Test
    void testIsEmpty() {
        assertTrue(emptyTree.isEmpty());
        assertFalse(tree.isEmpty());
    }
    
    @Test
    void testSize() {
        assertEquals(0, emptyTree.size());
        assertEquals(7, tree.size());
    }
    
    @Test
    void testContains() {
        assertFalse(emptyTree.contains(5));
        assertTrue(tree.contains(5));
        assertTrue(tree.contains(3));
        assertTrue(tree.contains(7));
        assertTrue(tree.contains(1));
        assertTrue(tree.contains(4));
        assertTrue(tree.contains(6));
        assertTrue(tree.contains(9));
        assertFalse(tree.contains(10));
        assertFalse(tree.contains(0));
        assertFalse(tree.contains(null));
    }
    
    @Test
    void testContainsElement() {
        assertTrue(tree.containsElement(5));
        assertFalse(tree.containsElement(10));
        assertFalse(tree.containsElement(null));
    }
    
    @Test
    void testToArray() {
        Object[] array = tree.toArray();
        assertEquals(7, array.length);
        List<Object> list = Arrays.asList(array);
        assertTrue(list.contains(5));
        assertTrue(list.contains(3));
        assertTrue(list.contains(7));
        assertTrue(list.contains(1));
        assertTrue(list.contains(4));
        assertTrue(list.contains(6));
        assertTrue(list.contains(9));
    }
    
    @Test
    void testToArrayWithType() {
        Integer[] array = tree.toArray(new Integer[0]);
        assertEquals(7, array.length);
        List<Integer> list = Arrays.asList(array);
        assertTrue(list.contains(5));
        assertTrue(list.contains(3));
        assertTrue(list.contains(7));
    }
    
    @Test
    void testIterator() {
        List<Integer> collected = new ArrayList<>();
        for (Integer value : tree) {
            collected.add(value);
        }
        
        assertEquals(7, collected.size());
        Collections.sort(collected); // Tree should return in-order
        assertEquals(Arrays.asList(1, 3, 4, 5, 6, 7, 9), collected);
    }
    
    @Test
    void testIteratorRemoveThrows() {
        Iterator<Integer> iterator = tree.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
    
    @Test
    void testIteratorEmptyTree() {
        Iterator<Integer> iterator = emptyTree.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
    
    @Test
    void testCreateWithAddedReturnsNewInstance() {
        PersistentBinaryTree<Integer> original = new PersistentBinaryTree<>();
        PersistentBinaryTree<Integer> modified = (PersistentBinaryTree<Integer>) original.createWithAdded(10);
        
        assertNotSame(original, modified);
        assertFalse(original.contains(10));
        assertTrue(modified.contains(10));
    }
    
    @Test
    void testCreateWithRemovedReturnsNewInstance() {
        PersistentBinaryTree<Integer> original = new PersistentBinaryTree<>();
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(10);
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(20);
        
        PersistentBinaryTree<Integer> modified = (PersistentBinaryTree<Integer>) original.createWithRemoved(10);
        
        assertNotSame(original, modified);
        assertTrue(original.contains(10));
        assertFalse(modified.contains(10));
        assertTrue(modified.contains(20));
    }
    
    @Test
    void testCreateWithRemovedNonExisting() {
        PersistentBinaryTree<Integer> original = new PersistentBinaryTree<>();
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(10);
        
        PersistentBinaryTree<Integer> modified = (PersistentBinaryTree<Integer>) original.createWithRemoved(99);
        
        assertSame(original, modified); // Should return same instance
    }
    
    @Test
    void testCreateEmpty() {
        PersistentBinaryTree<Integer> empty = (PersistentBinaryTree<Integer>) tree.createEmpty();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertNotSame(tree, empty);
    }
    
    @Test
    void testTreeSpecificMethods() {
        assertEquals(1, tree.min());
        assertEquals(9, tree.max());
        assertTrue(tree.height() > 0);
        assertTrue(tree.isBalanced());
    }
    
    @Test
    void testTreeSpecificMethodsEmptyTree() {
        assertThrows(NoSuchElementException.class, emptyTree::min);
        assertThrows(NoSuchElementException.class, emptyTree::max);
        assertEquals(0, emptyTree.height());
        assertTrue(emptyTree.isBalanced());
    }
    
    @Test
    void testMultipleVersions() {
        // Create version chain
        PersistentBinaryTree<Integer> v1 = new PersistentBinaryTree<>();
        
        PersistentBinaryTree<Integer> v2 = (PersistentBinaryTree<Integer>) v1.createWithAdded(10);
        PersistentBinaryTree<Integer> v3 = (PersistentBinaryTree<Integer>) v2.createWithAdded(20);
        PersistentBinaryTree<Integer> v4 = (PersistentBinaryTree<Integer>) v3.createWithAdded(30);
        
        // Remove from middle version
        PersistentBinaryTree<Integer> v5 = (PersistentBinaryTree<Integer>) v3.createWithRemoved(20);
        
        // All versions should be independent
        assertEquals(0, v1.size());
        assertEquals(1, v2.size());
        assertTrue(v2.contains(10));
        
        assertEquals(2, v3.size());
        assertTrue(v3.contains(10));
        assertTrue(v3.contains(20));
        
        assertEquals(3, v4.size());
        assertTrue(v4.contains(10));
        assertTrue(v4.contains(20));
        assertTrue(v4.contains(30));
        
        assertEquals(1, v5.size());
        assertTrue(v5.contains(10));
        assertFalse(v5.contains(20));
    }
    
    @Test
    void testImmutability() {
        // Create original tree
        PersistentBinaryTree<Integer> original = new PersistentBinaryTree<>();
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(1);
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(2);
        original = (PersistentBinaryTree<Integer>) original.createWithAdded(3);
        
        // Get snapshot
        PersistentBinaryTree<Integer> snapshot = (PersistentBinaryTree<Integer>) original.snapshot();
        
        // "Modify" original (actually create new version)
        PersistentBinaryTree<Integer> modified = (PersistentBinaryTree<Integer>) original.createWithAdded(4);
        
        // Snapshot should remain unchanged
        assertEquals(3, snapshot.size());
        assertTrue(snapshot.contains(1));
        assertTrue(snapshot.contains(2));
        assertTrue(snapshot.contains(3));
        assertFalse(snapshot.contains(4));
        
        // Modified should have new element
        assertEquals(4, modified.size());
        assertTrue(modified.contains(4));
        
        // Original should also remain unchanged (it's immutable)
        assertEquals(3, original.size());
        assertFalse(original.contains(4));
    }
    
    // ========== Tests for TransactionalPersistentBinaryTree (mutable wrapper) ==========
    
    @Test
    void testTransactionalAdd() {
        TransactionalPersistentBinaryTree<Integer> transactional = new TransactionalPersistentBinaryTree<>();
        
        // Test single add
        assertTrue(transactional.add(10));
        assertEquals(1, transactional.size());
        assertTrue(transactional.contains(10));
        
        // Test multiple adds
        assertTrue(transactional.add(5));
        assertTrue(transactional.add(15));
        assertEquals(3, transactional.size());
        assertTrue(transactional.contains(5));
        assertTrue(transactional.contains(15));
        
        // Test adding duplicate
        int sizeBefore = transactional.size();
        assertFalse(transactional.add(5)); // Add existing element
        assertEquals(sizeBefore, transactional.size());
    }
    
    @Test
    void testTransactionalRemove() {
        // Test remove existing element
        assertTrue(transactionalTree.remove(5));
        assertEquals(6, transactionalTree.size());
        assertFalse(transactionalTree.contains(5));
        
        // Test remove non-existing element
        assertFalse(transactionalTree.remove(100));
        assertEquals(6, transactionalTree.size());
        
        // Test remove with wrong type
        assertFalse(transactionalTree.remove("string"));
        assertEquals(6, transactionalTree.size());
    }
    
    @Test
    void testTransactionalAddAll() {
        TransactionalPersistentBinaryTree<Integer> transactional = new TransactionalPersistentBinaryTree<>();
        Collection<Integer> toAdd = Arrays.asList(10, 20, 30, 20); // Duplicate
        
        assertTrue(transactional.addAll(toAdd));
        assertEquals(3, transactional.size()); // Should ignore duplicate
        assertTrue(transactional.contains(10));
        assertTrue(transactional.contains(20));
        assertTrue(transactional.contains(30));
    }
    
    @Test
    void testTransactionalRemoveAll() {
        Collection<Integer> toRemove = Arrays.asList(3, 7, 100); // 100 doesn't exist
        
        assertTrue(transactionalTree.removeAll(toRemove));
        assertEquals(5, transactionalTree.size()); // Removed 3 and 7
        assertFalse(transactionalTree.contains(3));
        assertFalse(transactionalTree.contains(7));
    }
    
    @Test
    void testTransactionalRetainAll() {
        Collection<Integer> toRetain = Arrays.asList(3, 7, 100); // 100 doesn't exist
        
        assertTrue(transactionalTree.retainAll(toRetain));
        assertEquals(2, transactionalTree.size());
        assertTrue(transactionalTree.contains(3));
        assertTrue(transactionalTree.contains(7));
        assertFalse(transactionalTree.contains(1));
        assertFalse(transactionalTree.contains(4));
        assertFalse(transactionalTree.contains(5));
        assertFalse(transactionalTree.contains(6));
        assertFalse(transactionalTree.contains(9));
    }
    
    @Test
    void testTransactionalClear() {
        assertFalse(transactionalTree.isEmpty());
        transactionalTree.clear();
        assertTrue(transactionalTree.isEmpty());
        assertEquals(0, transactionalTree.size());
    }
    
    @Test
    void testTransactionalSnapshot() {
        // Get snapshot
        PersistentBinaryTree<Integer> snapshot = transactionalTree.snapshot();
        
        // Modify transactional tree
        transactionalTree.add(100);
        
        // Snapshot should remain unchanged
        assertEquals(7, snapshot.size());
        assertFalse(snapshot.contains(100));
        
        // Transactional tree should have new element
        assertEquals(8, transactionalTree.size());
        assertTrue(transactionalTree.contains(100));
    }
    
    @Test
    void testTransactionalTreeSpecificMethods() {
        assertEquals(1, transactionalTree.min());
        assertEquals(9, transactionalTree.max());
        assertTrue(transactionalTree.height() > 0);
        assertTrue(transactionalTree.isBalanced());
    }
    
    @Test
    void testLargeNumberOfElements() {
        TransactionalPersistentBinaryTree<Integer> large = new TransactionalPersistentBinaryTree<>();
        int count = 1000;
        
        for (int i = 0; i < count; i++) {
            large.add(i);
        }
        
        assertEquals(count, large.size());
        
        // Verify all elements are present
        for (int i = 0; i < count; i++) {
            assertTrue(large.contains(i));
        }
        
        // Verify tree is still balanced
        assertTrue(large.isBalanced());
    }
    
    @Test
    void testTransactionalEqualsAndHashCode() {
        TransactionalPersistentBinaryTree<Integer> tree1 = new TransactionalPersistentBinaryTree<>();
        tree1.add(1);
        tree1.add(2);
        tree1.add(3);
        
        // Get underlying persistent tree
        PersistentBinaryTree<Integer> persistent1 = tree1.snapshot();
        
        TransactionalPersistentBinaryTree<Integer> tree2 = new TransactionalPersistentBinaryTree<>();
        tree2.add(3);
        tree2.add(1);
        tree2.add(2);
        
        PersistentBinaryTree<Integer> persistent2 = tree2.snapshot();
        
        // Persistent trees should be equal (same elements)
        assertEquals(persistent1, persistent2);
        assertEquals(persistent1.hashCode(), persistent2.hashCode());
    }
    
    @Test
    void testToString() {
        TransactionalPersistentBinaryTree<Integer> tree = new TransactionalPersistentBinaryTree<>();
        tree.add(2);
        tree.add(1);
        tree.add(3);
        
        String str = tree.toString();
        // Should be in order: 1, 2, 3
        assertTrue(str.contains("1") && str.contains("2") && str.contains("3"));
        assertTrue(str.startsWith("[") && str.endsWith("]"));
    }
    
    @Test
    void testStreamSupport() {
        List<Integer> collected = transactionalTree.stream()
            .filter(x -> x > 5)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(6, 7, 9), collected);
    }
}