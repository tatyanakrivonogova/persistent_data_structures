package persistent.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TransactionalPersistentBinaryTree.
 */
class TransactionalPersistentBinaryTreeTest {
    private TransactionalPersistentBinaryTree<Integer> emptyTree;
    private TransactionalPersistentBinaryTree<Integer> treeWithElements;
    
    @BeforeEach
    void setUp() {
        emptyTree = new TransactionalPersistentBinaryTree<>();
        treeWithElements = new TransactionalPersistentBinaryTree<>();
        treeWithElements.add(2);
        treeWithElements.add(1);
        treeWithElements.add(3);
    }
    
    @Test
    @DisplayName("Transactional add and remove")
    void testTransactionalAddRemove() {
        assertTrue(treeWithElements.add(4));
        assertEquals(4, treeWithElements.size());
        assertTrue(treeWithElements.contains(4));
        
        assertTrue(treeWithElements.remove(Integer.valueOf(2)));
        assertEquals(3, treeWithElements.size());
        assertFalse(treeWithElements.contains(2));
        
        assertFalse(treeWithElements.remove(Integer.valueOf(99))); // Non-existent
        assertEquals(3, treeWithElements.size());
    }
    
    @Test
    @DisplayName("Add duplicate element should return false")
    void testTransactionalAddDuplicate() {
        assertFalse(treeWithElements.add(2)); // Уже существует
        assertEquals(3, treeWithElements.size());
    }
    
    @Test
    @DisplayName("Transactional contains")
    void testTransactionalContains() {
        assertTrue(treeWithElements.contains(1));
        assertTrue(treeWithElements.contains(2));
        assertTrue(treeWithElements.contains(3));
        assertFalse(treeWithElements.contains(4));
        assertFalse(treeWithElements.contains("string")); // Wrong type
    }
    
    @Test
    @DisplayName("Transactional iterator returns sorted elements")
    void testTransactionalIterator() {
        List<Integer> collected = new ArrayList<>();
        for (Integer element : treeWithElements) {
            collected.add(element);
        }
        
        assertEquals(3, collected.size());
        assertEquals(List.of(1, 2, 3), collected); // Отсортированный порядок
    }
    
    @Test
    @DisplayName("Transactional toArray")
    void testTransactionalToArray() {
        Object[] array = treeWithElements.toArray();
        assertArrayEquals(new Object[]{1, 2, 3}, array);
        
        Integer[] typedArray = treeWithElements.toArray(new Integer[0]);
        assertArrayEquals(new Integer[]{1, 2, 3}, typedArray);
    }
    
    @Test
    @DisplayName("Transactional addAll and removeAll")
    void testTransactionalBulkOperations() {
        List<Integer> toAdd = Arrays.asList(4, 5, 6);
        assertTrue(treeWithElements.addAll(toAdd));
        assertEquals(6, treeWithElements.size());
        assertTrue(treeWithElements.contains(4));
        assertTrue(treeWithElements.contains(5));
        assertTrue(treeWithElements.contains(6));
        
        List<Integer> toRemove = Arrays.asList(2, 4, 6);
        assertTrue(treeWithElements.removeAll(toRemove));
        assertEquals(3, treeWithElements.size());
        assertFalse(treeWithElements.contains(2));
        assertFalse(treeWithElements.contains(4));
        assertFalse(treeWithElements.contains(6));
        assertTrue(treeWithElements.contains(1));
        assertTrue(treeWithElements.contains(3));
        assertTrue(treeWithElements.contains(5));
    }
    
    @Test
    @DisplayName("Transactional retainAll")
    void testTransactionalRetainAll() {
        List<Integer> toRetain = Arrays.asList(2, 3);
        assertTrue(treeWithElements.retainAll(toRetain)); // Должны быть изменения (удалили 1)
        assertEquals(2, treeWithElements.size());
        assertFalse(treeWithElements.contains(1));
        assertTrue(treeWithElements.contains(2));
        assertTrue(treeWithElements.contains(3));
        
        // No change if already retaining - теперь должно вернуть false
        assertFalse(treeWithElements.retainAll(toRetain));
    }
    
    @Test
    @DisplayName("Transactional clear")
    void testTransactionalClear() {
        treeWithElements.clear();
        assertTrue(treeWithElements.isEmpty());
        assertEquals(0, treeWithElements.size());
    }
    
    @Test
    @DisplayName("Snapshot functionality")
    void testSnapshot() {
        PersistentBinaryTree<Integer> snapshot = treeWithElements.snapshot();
        assertNotNull(snapshot);
        assertEquals(3, snapshot.size());
        assertTrue(snapshot.containsElement(1));
        
        // Modify transactional tree
        treeWithElements.add(4);
        assertEquals(4, treeWithElements.size());
        
        // Snapshot should remain unchanged
        assertEquals(3, snapshot.size());
        assertFalse(snapshot.containsElement(4));
    }
    
    @Test
    @DisplayName("Transactional copy")
    void testTransactionalCopy() {
        TransactionalPersistentBinaryTree<Integer> copy = 
            treeWithElements.transactionalCopy();
        
        // Initially they should be equal
        assertEquals(treeWithElements.size(), copy.size());
        assertTrue(copy.containsAll(treeWithElements));
        
        // Modify original
        treeWithElements.add(4);
        
        // Copy should remain unchanged
        assertEquals(3, copy.size());
        assertFalse(copy.contains(4));
        
        // Modify copy independently
        copy.add(5);
        assertEquals(4, copy.size());
        assertTrue(copy.contains(5));
        
        // Original unchanged by copy modification
        assertEquals(4, treeWithElements.size());
        assertFalse(treeWithElements.contains(5));
    }
    
    @Test
    @DisplayName("Tree-specific transactional methods")
    void testTreeSpecificTransactionalMethods() {
        TransactionalPersistentBinaryTree<Integer> tree = 
            new TransactionalPersistentBinaryTree<>();
        
        tree.add(50);
        tree.add(25);
        tree.add(75);
        tree.add(10);
        tree.add(30);
        
        assertEquals(Integer.valueOf(10), tree.min());
        assertEquals(Integer.valueOf(75), tree.max());
        assertTrue(tree.height() > 0);
        assertTrue(tree.isBalanced());
    }
    
    @Test
    @DisplayName("Concurrent modification test")
    void testConcurrentModifications() throws InterruptedException {
        final TransactionalPersistentBinaryTree<Integer> sharedTree = 
            new TransactionalPersistentBinaryTree<>();
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        int value = threadId * operationsPerThread + j;
                        sharedTree.add(value);
                        
                        // Occasionally remove
                        if (j % 10 == 0 && !sharedTree.isEmpty()) {
                            sharedTree.remove(sharedTree.min());
                        }
                    }
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // All threads should complete successfully
        assertEquals(threadCount, successCount.get());
        
        // Tree should be in consistent state and balanced
        assertTrue(sharedTree.isBalanced());
        
        // All elements should be unique
        Set<Integer> elements = new HashSet<>();
        for (Integer element : sharedTree) {
            assertTrue(elements.add(element), "Duplicate element found: " + element);
        }
        assertEquals(elements.size(), sharedTree.size());
    }
    
    @Test
    @DisplayName("Stress test with many operations")
    void testStressTest() {
        TransactionalPersistentBinaryTree<Integer> tree = 
            new TransactionalPersistentBinaryTree<>();
        
        // Add many elements
        for (int i = 0; i < 1000; i++) {
            tree.add(i);
        }
        assertEquals(1000, tree.size());
        assertTrue(tree.isBalanced());
        
        // Remove half
        for (int i = 0; i < 500; i++) {
            tree.remove(i);
        }
        assertEquals(500, tree.size());
        assertTrue(tree.isBalanced());
        
        // Add more
        for (int i = 1000; i < 1500; i++) {
            tree.add(i);
        }
        assertEquals(1000, tree.size());
        assertTrue(tree.isBalanced());
        
        // Verify contents
        for (int i = 500; i < 1500; i++) {
            assertTrue(tree.contains(i));
        }
        
        for (int i = 0; i < 500; i++) {
            assertFalse(tree.contains(i));
        }
        
        // Verify min and max
        assertEquals(Integer.valueOf(500), tree.min());
        assertEquals(Integer.valueOf(1499), tree.max());
    }
    
    @Test
    @DisplayName("Empty transactional tree operations")
    void testEmptyTransactionalTree() {
        assertTrue(emptyTree.isEmpty());
        assertEquals(0, emptyTree.size());
        assertFalse(emptyTree.iterator().hasNext());
        assertArrayEquals(new Object[0], emptyTree.toArray());
        
        // Operations on empty tree should return false
        assertFalse(emptyTree.remove(Integer.valueOf(1)));
        assertFalse(emptyTree.removeAll(Arrays.asList(1, 2)));
        assertFalse(emptyTree.retainAll(Arrays.asList(1, 2))); // retainAll на пустом дереве должен вернуть false
        
        // Tree-specific operations should throw
        assertThrows(NoSuchElementException.class, () -> emptyTree.min());
        assertThrows(NoSuchElementException.class, () -> emptyTree.max());
        
        // Но height и isBalanced должны работать
        assertEquals(0, emptyTree.height());
        assertTrue(emptyTree.isBalanced());
    }
    
    @Test
    @DisplayName("Concurrent snapshot consistency")
    void testConcurrentSnapshotConsistency() throws InterruptedException, ExecutionException {
        final TransactionalPersistentBinaryTree<Integer> tree = 
            new TransactionalPersistentBinaryTree<>();
        
        // Start adding elements in background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                tree.add(i);
                Thread.yield(); // Allow interruptions
            }
        });
        
        // Take snapshots while adding
        List<PersistentBinaryTree<Integer>> snapshots = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            snapshots.add(tree.snapshot());
            Thread.sleep(1); // Small delay
        }
        
        future.get(); // Wait for completion
        executor.shutdown();
        
        // All snapshots should be in consistent state
        for (PersistentBinaryTree<Integer> snapshot : snapshots) {
            // Each snapshot should be a valid tree
            assertNotNull(snapshot);
            assertTrue(snapshot.isBalanced());
            
            // All elements in snapshot should be unique and sorted
            Integer prev = null;
            for (Integer element : snapshot) {
                if (prev != null) {
                    assertTrue(prev < element, "Elements not sorted: " + prev + " >= " + element);
                }
                prev = element;
            }
        }
    }
    
    @Test
    @DisplayName("Tree maintains balance during transactional operations")
    void testBalanceDuringTransactions() {
        TransactionalPersistentBinaryTree<Integer> tree = 
            new TransactionalPersistentBinaryTree<>();
        
        // Insert in reverse order (worst case for BST)
        for (int i = 100; i >= 0; i--) {
            tree.add(i);
            assertTrue(tree.isBalanced(), "Tree unbalanced after adding " + i);
        }
        
        // Remove in random order
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            toRemove.add(i);
        }
        Collections.shuffle(toRemove);
        
        for (Integer element : toRemove) {
            tree.remove(element);
            assertTrue(tree.isBalanced(), "Tree unbalanced after removing " + element);
        }
        
        assertTrue(tree.isEmpty());
    }
}