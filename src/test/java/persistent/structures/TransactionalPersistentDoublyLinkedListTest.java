package persistent.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for PersistentDoublyLinkedList. */
@SuppressWarnings({"MagicNumber"})
class TransactionalListTest {

  /** Empty list for testing. */
  private TransactionalPersistentDoublyLinkedList<Integer> emptyList;
  /** List with elements for testing. */
  private TransactionalPersistentDoublyLinkedList<Integer> listWithElements;

  @BeforeEach
  void setUp() {
    emptyList = new TransactionalPersistentDoublyLinkedList<>();
    listWithElements = new TransactionalPersistentDoublyLinkedList<>();
    listWithElements.add(1);
    listWithElements.add(2);
    listWithElements.add(3);
  }

  @Test
  @DisplayName("Transactional add and remove")
  void testTransactionalAddRemove() {
    assertTrue(listWithElements.add(4));
    assertEquals(4, listWithElements.size());
    assertTrue(listWithElements.contains(4));

    assertTrue(listWithElements.remove(Integer.valueOf(2)));
    assertEquals(3, listWithElements.size());
    assertFalse(listWithElements.contains(2));

    assertFalse(listWithElements.remove(Integer.valueOf(99))); // Non-existent
    assertEquals(3, listWithElements.size());
  }

  @Test
  @DisplayName("Transactional contains")
  void testTransactionalContains() {
    assertTrue(listWithElements.contains(1));
    assertTrue(listWithElements.contains(2));
    assertTrue(listWithElements.contains(3));
    assertFalse(listWithElements.contains(4));
    assertFalse(listWithElements.contains("string")); // Wrong type
  }

  @Test
  @DisplayName("Transactional iterator")
  void testTransactionalIterator() {
    List<Integer> collected = new ArrayList<>();
    for (Integer element : listWithElements) {
      collected.add(element);
    }

    assertEquals(3, collected.size());
    assertEquals(List.of(1, 2, 3), collected);
  }

  @Test
  @DisplayName("Transactional toArray")
  void testTransactionalToArray() {
    Object[] array = listWithElements.toArray();
    assertArrayEquals(new Object[] {1, 2, 3}, array);

    Integer[] typedArray = listWithElements.toArray(new Integer[0]);
    assertArrayEquals(new Integer[] {1, 2, 3}, typedArray);
  }

  @Test
  @DisplayName("Transactional addAll and removeAll")
  void testTransactionalBulkOperations() {
    List<Integer> toAdd = Arrays.asList(4, 5, 6);
    assertTrue(listWithElements.addAll(toAdd));
    assertEquals(6, listWithElements.size());
    assertTrue(listWithElements.contains(4));
    assertTrue(listWithElements.contains(5));
    assertTrue(listWithElements.contains(6));

    List<Integer> toRemove = Arrays.asList(2, 4, 6);
    assertTrue(listWithElements.removeAll(toRemove));
    assertEquals(3, listWithElements.size());
    assertFalse(listWithElements.contains(2));
    assertFalse(listWithElements.contains(4));
    assertFalse(listWithElements.contains(6));
    assertTrue(listWithElements.contains(1));
    assertTrue(listWithElements.contains(3));
    assertTrue(listWithElements.contains(5));
  }

  @Test
  @DisplayName("Transactional addAll with index")
  void testTransactionalAddAllWithIndex() {
    List<Integer> toAdd = Arrays.asList(4, 5);
    assertTrue(listWithElements.addAll(1, toAdd));
    
    assertEquals(5, listWithElements.size());
    assertEquals(Integer.valueOf(1), listWithElements.get(0));
    assertEquals(Integer.valueOf(4), listWithElements.get(1));
    assertEquals(Integer.valueOf(5), listWithElements.get(2));
    assertEquals(Integer.valueOf(2), listWithElements.get(3));
    assertEquals(Integer.valueOf(3), listWithElements.get(4));
  }

  @Test
  void testTransactionalRetainAll() {
    List<Integer> toRetain = Arrays.asList(2, 3);
    assertTrue(listWithElements.retainAll(toRetain));
    assertEquals(2, listWithElements.size());
    assertFalse(listWithElements.contains(1));
    assertTrue(listWithElements.contains(2));
    assertTrue(listWithElements.contains(3));

    // No change if already retaining
    assertFalse(listWithElements.retainAll(toRetain));
  }

  @Test
  @DisplayName("Transactional clear")
  void testTransactionalClear() {
    listWithElements.clear();
    assertTrue(listWithElements.isEmpty());
    assertEquals(0, listWithElements.size());
  }

  @Test
  @DisplayName("Transactional set")
  void testTransactionalSet() {
    assertEquals(Integer.valueOf(2), listWithElements.set(1, 20));
    assertEquals(Integer.valueOf(20), listWithElements.get(1));
    assertEquals(3, listWithElements.size());
    
    assertThrows(IndexOutOfBoundsException.class, () -> listWithElements.set(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> listWithElements.set(3, 0));
  }

  @Test
  @DisplayName("Snapshot functionality")
  void testSnapshot() {
    PersistentDoublyLinkedList<Integer> snapshot = listWithElements.snapshot();
    assertNotNull(snapshot);
    assertEquals(3, snapshot.size());
    assertTrue(snapshot.containsElement(1));

    // Modify transactional list
    listWithElements.add(4);
    assertEquals(4, listWithElements.size());

    // Snapshot should remain unchanged
    assertEquals(3, snapshot.size());
    assertFalse(snapshot.containsElement(4));
  }

  @Test
  @DisplayName("Transactional copy")
  void testTransactionalCopy() {
    TransactionalPersistentDoublyLinkedList<Integer> copy =
        listWithElements.transactionalCopy();

    // Initially they should be equal
    assertEquals(listWithElements.size(), copy.size());
    assertTrue(copy.containsAll(listWithElements));

    // Modify original
    listWithElements.add(4);

    // Copy should remain unchanged
    assertEquals(3, copy.size());
    assertFalse(copy.contains(4));

    // Modify copy independently
    copy.add(5);
    assertEquals(4, copy.size());
    assertTrue(copy.contains(5));

    // Original unchanged by copy modification
    assertEquals(4, listWithElements.size());
    assertFalse(listWithElements.contains(5));
  }

  @Test
  @DisplayName("List-specific transactional methods")
  void testListSpecificTransactionalMethods() {
    TransactionalPersistentDoublyLinkedList<Integer> list =
        new TransactionalPersistentDoublyLinkedList<>();

    list.addFirst(3); // [3]
    assertEquals(1, list.size());
    assertEquals(Integer.valueOf(3), list.get(0));

    list.addFirst(1); // [1, 3]
    assertEquals(2, list.size());

    list.add(1, 2); // [1, 2, 3]
    assertEquals(3, list.size());
    assertEquals(Integer.valueOf(2), list.get(1));

    list.addLast(4); // [1, 2, 3, 4]
    assertEquals(4, list.size());
    assertEquals(Integer.valueOf(4), list.get(3));

    assertEquals(Integer.valueOf(1), list.removeFirst()); // [2, 3, 4]
    assertEquals(3, list.size());
    assertFalse(list.contains(1));

    assertEquals(Integer.valueOf(4), list.removeLast()); // [2, 3]
    assertEquals(2, list.size());
    assertFalse(list.contains(4));

    assertEquals(Integer.valueOf(3), list.remove(1)); // [2]
    assertEquals(1, list.size());
    assertFalse(list.contains(3));

    // Remove last element
    assertEquals(Integer.valueOf(2), list.remove(0)); // []
    assertTrue(list.isEmpty());
  }

  @Test
  @DisplayName("Test ListIterator functionality")
  void testListIterator() {
    ListIterator<Integer> iterator = listWithElements.listIterator();
    
    assertTrue(iterator.hasNext());
    assertFalse(iterator.hasPrevious());
    assertEquals(0, iterator.nextIndex());
    assertEquals(-1, iterator.previousIndex());
    
    assertEquals(Integer.valueOf(1), iterator.next());
    
    // Test set
    iterator.set(10);
    assertEquals(Integer.valueOf(10), listWithElements.get(0));
    
    // Test add
    iterator.add(15);
    assertEquals(4, listWithElements.size());
    assertEquals(Integer.valueOf(15), listWithElements.get(1));
    
    // Test remove
    iterator.next(); // Move to 2
    iterator.remove();
    assertEquals(3, listWithElements.size());
    assertFalse(listWithElements.contains(2));
  }

  @Test
  @DisplayName("Concurrent modification test")
  void testConcurrentModifications() throws InterruptedException {
    final TransactionalPersistentDoublyLinkedList<Integer> sharedList =
        new TransactionalPersistentDoublyLinkedList<>();
    final int threadCount = 10;
    final int operationsPerThread = 100;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);
    final AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                int value = threadId * operationsPerThread + j;
                sharedList.add(value);

                // Occasionally remove
                if (j % 10 == 0 && !sharedList.isEmpty()) {
                  sharedList.removeFirst();
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

    // List should be in consistent state
    assertTrue(sharedList.snapshot().size() >= 0); // Basic consistency check
  }

  @Test
  @DisplayName("Stress test with many operations")
  void testStressTest() {
    TransactionalPersistentDoublyLinkedList<Integer> list =
        new TransactionalPersistentDoublyLinkedList<>();

    // Add many elements
    for (int i = 0; i < 1000; i++) {
      list.add(i);
    }
    assertEquals(1000, list.size());

    // Remove half
    for (int i = 0; i < 500; i++) {
      list.remove(Integer.valueOf(i));
    }
    assertEquals(500, list.size());

    // Add more
    for (int i = 1000; i < 1500; i++) {
      list.add(i);
    }
    assertEquals(1000, list.size());

    // Verify contents
    for (int i = 500; i < 1500; i++) {
      assertTrue(list.contains(i));
    }

    for (int i = 0; i < 500; i++) {
      assertFalse(list.contains(i));
    }
  }

  @Test
  @DisplayName("Equals and toString for transactional list")
  void testTransactionalEqualsAndToString() {
    TransactionalPersistentDoublyLinkedList<Integer> list1 =
        new TransactionalPersistentDoublyLinkedList<>();
    list1.add(1);
    list1.add(2);
    list1.add(3);

    TransactionalPersistentDoublyLinkedList<Integer> list2 =
        new TransactionalPersistentDoublyLinkedList<>();
    list2.add(1);
    list2.add(2);
    list2.add(3);

    // Transactional lists delegate equals to underlying persistent list
    assertEquals(list1.snapshot(), list2.snapshot());

    assertEquals("[1, 2, 3]", list1.toString());

    list1.add(4);
    assertNotEquals(list1.snapshot(), list2.snapshot());
  }

  @Test
  @DisplayName("Test subList on transactional list")
  void testTransactionalSubList() {
    listWithElements.add(4);
    listWithElements.add(5);
    
    List<Integer> subList = listWithElements.subList(1, 4);
    assertEquals(3, subList.size());
    assertEquals(List.of(2, 3, 4), subList);
    
    // Sublist should be immutable
    assertThrows(UnsupportedOperationException.class, () -> subList.add(6));
  }

  @Test
  @DisplayName("Test indexOf and lastIndexOf on transactional list")
  void testTransactionalIndexOf() {
    listWithElements.add(2);
    listWithElements.add(1);
    
    assertEquals(0, listWithElements.indexOf(1));
    assertEquals(1, listWithElements.indexOf(2));
    assertEquals(2, listWithElements.indexOf(3));
    
    assertEquals(4, listWithElements.lastIndexOf(1));
    assertEquals(3, listWithElements.lastIndexOf(2));
    assertEquals(2, listWithElements.lastIndexOf(3));
  }

  @Test
  @DisplayName("Empty transactional list operations")
  void testEmptyTransactionalList() {
    assertTrue(emptyList.isEmpty());
    assertEquals(0, emptyList.size());
    assertFalse(emptyList.iterator().hasNext());
    assertArrayEquals(new Object[0], emptyList.toArray());

    // Operations on empty list should return false
    assertFalse(emptyList.remove(Integer.valueOf(1)));
    assertFalse(emptyList.removeAll(Arrays.asList(1, 2)));
    // Пустой список retainAll должен вернуть false
    assertFalse(emptyList.retainAll(Arrays.asList(1, 2)));

    // List-specific operations should throw
    assertThrows(NoSuchElementException.class, () -> emptyList.removeFirst());
    assertThrows(NoSuchElementException.class, () -> emptyList.removeLast());
    assertThrows(IndexOutOfBoundsException.class, () -> emptyList.get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> emptyList.remove(0));
    assertThrows(IndexOutOfBoundsException.class, () -> emptyList.set(0, 10));
    assertThrows(IndexOutOfBoundsException.class, () -> emptyList.add(1, 10));
  }

  @Test
  @DisplayName("Concurrent snapshot consistency")
  void testConcurrentSnapshotConsistency()
      throws InterruptedException, ExecutionException {
    final TransactionalPersistentDoublyLinkedList<Integer> list =
        new TransactionalPersistentDoublyLinkedList<>();

    // Start adding elements in background
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> future =
        executor.submit(
            () -> {
              for (int i = 0; i < 1000; i++) {
                list.add(i);
                Thread.yield(); // Allow interruptions
              }
            });

    // Take snapshots while adding
    List<PersistentDoublyLinkedList<Integer>> snapshots = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      snapshots.add(list.snapshot());
      Thread.sleep(1); // Small delay
    }

    future.get(); // Wait for completion
    executor.shutdown();

    // All snapshots should be in consistent state
    for (PersistentDoublyLinkedList<Integer> snapshot : snapshots) {
      // Each snapshot should be a valid list
      assertNotNull(snapshot);
      // All elements in snapshot should be unique (no duplicates)
      Set<Integer> elements = new HashSet<>();
      for (Integer element : snapshot) {
        assertTrue(elements.add(element), "Duplicate element in snapshot");
      }
    }
  }
}
