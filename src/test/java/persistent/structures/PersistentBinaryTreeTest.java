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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Comprehensive tests for PersistentBinaryTree. */
@DisplayName("Persistent Binary Tree Tests")
class PersistentBinaryTreeTest {

  /** Empty tree for testing. */
  private PersistentBinaryTree<Integer> emptyTree;
  /** Balanced tree for testing. */
  private PersistentBinaryTree<Integer> balancedTree;
  /** Unbalanced tree for testing. */
  private PersistentBinaryTree<Integer> unbalancedTree;

  @BeforeEach
  void setUp() {
    emptyTree = new PersistentBinaryTree<>();

    // Сбалансированное дерево: [2, 1, 3]
    balancedTree =
        (PersistentBinaryTree<Integer>)
            emptyTree.createWithAdded(2).createWithAdded(1).createWithAdded(3);

    // Несбалансированное дерево: [1, 2, 3, 4, 5] (вырождается в список)
    unbalancedTree =
        (PersistentBinaryTree<Integer>)
            emptyTree
                .createWithAdded(1)
                .createWithAdded(2)
                .createWithAdded(3)
                .createWithAdded(4)
                .createWithAdded(5);
  }

  @Test
  @DisplayName("Empty tree should be empty")
  void testEmptyTree() {
    assertTrue(emptyTree.isEmpty());
    assertEquals(0, emptyTree.size());
    assertFalse(emptyTree.iterator().hasNext());
  }

  @Test
  @DisplayName("Add elements to tree")
  void testAddElements() {
    PersistentBinaryTree<Integer> tree =
        (PersistentBinaryTree<Integer>) emptyTree.createWithAdded(10);
    assertFalse(tree.isEmpty());
    assertEquals(1, tree.size());
    assertTrue(tree.containsElement(10));

    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(5);
    assertEquals(2, tree.size());
    assertTrue(tree.containsElement(5));
    assertTrue(tree.containsElement(10));

    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(15);
    assertEquals(3, tree.size());
    assertTrue(tree.containsElement(5));
    assertTrue(tree.containsElement(10));
    assertTrue(tree.containsElement(15));
  }

  @Test
  @DisplayName("Add duplicate element should return same tree")
  void testAddDuplicate() {
    PersistentBinaryTree<Integer> original = balancedTree;
    PersistentBinaryTree<Integer> modified =
        (PersistentBinaryTree<Integer>) original.createWithAdded(2);
    assertSame(original, modified);
  }

  @Test
  @DisplayName("Remove elements from tree")
  void testRemoveElements() {
    PersistentBinaryTree<Integer> tree =
        (PersistentBinaryTree<Integer>) balancedTree.createWithRemoved(2);
    assertEquals(2, tree.size());
    assertTrue(tree.containsElement(1));
    assertFalse(tree.containsElement(2));
    assertTrue(tree.containsElement(3));

    tree = (PersistentBinaryTree<Integer>) tree.createWithRemoved(1);
    assertEquals(1, tree.size());
    assertFalse(tree.containsElement(1));
    assertTrue(tree.containsElement(3));

    tree = (PersistentBinaryTree<Integer>) tree.createWithRemoved(3);
    assertTrue(tree.isEmpty());
  }

  @Test
  @DisplayName("Remove non-existent element should return same tree")
  void testRemoveNonExistent() {
    PersistentBinaryTree<Integer> original = balancedTree;
    PersistentBinaryTree<Integer> modified =
        (PersistentBinaryTree<Integer>) original.createWithRemoved(99);
    assertSame(original, modified);
  }

  @Test
  @DisplayName("Create empty tree")
  void testCreateEmpty() {
    PersistentBinaryTree<Integer> empty =
        (PersistentBinaryTree<Integer>) balancedTree.createEmpty();
    assertTrue(empty.isEmpty());
    assertEquals(0, empty.size());
  }

  @Test
  @DisplayName("Check contains element")
  void testContainsElement() {
    assertTrue(balancedTree.containsElement(1));
    assertTrue(balancedTree.containsElement(2));
    assertTrue(balancedTree.containsElement(3));
    assertFalse(balancedTree.containsElement(4));
    assertFalse(balancedTree.containsElement(null));
  }

  @Test
  @DisplayName("Collection contains method")
  void testCollectionContains() {
    assertTrue(balancedTree.contains(1));
    assertTrue(balancedTree.contains(2));
    assertTrue(balancedTree.contains(3));
    assertFalse(balancedTree.contains(4));
    assertFalse(balancedTree.contains("string")); // Wrong type
  }

  @Test
  @DisplayName("Iterator works correctly and returns sorted elements")
  void testIterator() {
    List<Integer> collected = new ArrayList<>();
    for (Integer element : balancedTree) {
      collected.add(element);
    }

    assertEquals(3, collected.size());
    assertEquals(List.of(1, 2, 3), collected); // In-order traversal should be sorted
  }

  @Test
  @DisplayName("Iterator should not support remove")
  void testIteratorRemove() {
    Iterator<Integer> iterator = balancedTree.iterator();
    iterator.next();
    assertThrows(UnsupportedOperationException.class, iterator::remove);
  }

  @Test
  @DisplayName("To array methods")
  void testToArray() {
    Object[] array = balancedTree.toArray();
    assertArrayEquals(new Object[] {1, 2, 3}, array);

    Integer[] typedArray = balancedTree.toArray(new Integer[0]);
    assertArrayEquals(new Integer[] {1, 2, 3}, typedArray);

    Integer[] largerArray = new Integer[5];
    Integer[] result = balancedTree.toArray(largerArray);
    assertSame(largerArray, result);
    assertArrayEquals(new Integer[] {1, 2, 3, null, null}, result);
  }

  @Test
  @DisplayName("Contains all elements")
  void testContainsAll() {
    List<Integer> checkList = Arrays.asList(1, 3);
    assertTrue(balancedTree.containsAll(checkList));

    List<Integer> notAllList = Arrays.asList(1, 4);
    assertFalse(balancedTree.containsAll(notAllList));
  }

  @Test
  @DisplayName("Immutable operations throw exceptions")
  void testImmutableOperations() {
    assertThrows(UnsupportedOperationException.class, () -> balancedTree.add(4));
    assertThrows(UnsupportedOperationException.class, () -> balancedTree.remove(2));
    assertThrows(UnsupportedOperationException.class,
        () -> balancedTree.addAll(Arrays.asList(4, 5)));
    assertThrows(UnsupportedOperationException.class,
        () -> balancedTree.removeAll(Arrays.asList(1, 2)));
    assertThrows(UnsupportedOperationException.class,
        () -> balancedTree.retainAll(Arrays.asList(1)));
    assertThrows(UnsupportedOperationException.class, () -> balancedTree.clear());
  }

  @Test
  @DisplayName("Equals and hashCode")
  void testEqualsAndHashCode() {
    PersistentBinaryTree<Integer> sameElements =
        (PersistentBinaryTree<Integer>)
            emptyTree.createWithAdded(2).createWithAdded(1).createWithAdded(3);

    PersistentBinaryTree<Integer> differentElements =
        (PersistentBinaryTree<Integer>) emptyTree.createWithAdded(4).createWithAdded(5);

    // Структурно разные деревья с одинаковыми элементами должны быть равны
    PersistentBinaryTree<Integer> differentlyBuilt =
        (PersistentBinaryTree<Integer>)
            emptyTree.createWithAdded(3).createWithAdded(1).createWithAdded(2);

    assertEquals(balancedTree, sameElements);
    assertEquals(balancedTree.hashCode(), sameElements.hashCode());

    assertNotEquals(balancedTree, differentElements);
    assertEquals(balancedTree, differentlyBuilt); // Должны быть равны по содержимому

    // Different type collection with same elements
    List<Integer> arrayList = Arrays.asList(1, 2, 3);
    assertEquals(balancedTree, arrayList);

    // Self equality
    assertEquals(balancedTree, balancedTree);

    // Null comparison
    assertNotEquals(null, balancedTree);

    // Different size
    PersistentBinaryTree<Integer> smaller =
        (PersistentBinaryTree<Integer>) emptyTree.createWithAdded(1);
    assertNotEquals(balancedTree, smaller);
  }

  @Test
  @DisplayName("Snapshot returns same instance")
  void testSnapshot() {
    assertSame(balancedTree, balancedTree.snapshot());
  }

  @Test
  @DisplayName("Get version")
  void testGetVersion() {
    assertNotNull(balancedTree.getVersion());
    assertNotNull(emptyTree.getVersion());
  }

  @Test
  @DisplayName("Tree-specific methods: min and max")
  void testTreeSpecificMinMax() {
    assertEquals(Integer.valueOf(1), balancedTree.min());
    assertEquals(Integer.valueOf(3), balancedTree.max());

    // Larger tree
    PersistentBinaryTree<Integer> largeTree =
        (PersistentBinaryTree<Integer>)
            emptyTree
                .createWithAdded(50)
                .createWithAdded(25)
                .createWithAdded(75)
                .createWithAdded(10)
                .createWithAdded(30)
                .createWithAdded(60)
                .createWithAdded(90);

    assertEquals(Integer.valueOf(10), largeTree.min());
    assertEquals(Integer.valueOf(90), largeTree.max());
  }

  @Test
  @DisplayName("Tree-specific methods: height and balance")
  void testTreeSpecificHeightAndBalance() {
    assertEquals(2, balancedTree.height());
    assertTrue(balancedTree.isBalanced());

    // Пустое дерево
    assertEquals(0, emptyTree.height());
    assertTrue(emptyTree.isBalanced());
  }

  @Test
  @DisplayName("Min/Max on empty tree throws exception")
  void testMinMaxEmptyTree() {
    assertThrows(NoSuchElementException.class, () -> emptyTree.min());
    assertThrows(NoSuchElementException.class, () -> emptyTree.max());
  }

  @Test
  @DisplayName("ToString method")
  void testToString() {
    assertEquals("[]", emptyTree.toString());
    assertEquals("[1, 2, 3]", balancedTree.toString());

    PersistentBinaryTree<String> stringTree =
        (PersistentBinaryTree<String>)
            new PersistentBinaryTree<String>()
                .createWithAdded("c")
                .createWithAdded("a")
                .createWithAdded("b");
    assertEquals("[a, b, c]", stringTree.toString());
  }

  @Test
  @DisplayName("Persistence: original tree unchanged after modifications")
  void testPersistence() {
    PersistentBinaryTree<Integer> original = balancedTree;
    PersistentBinaryTree<Integer> modified =
        (PersistentBinaryTree<Integer>) original.createWithAdded(4);

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
  @DisplayName("Complex sequence of operations maintains balance")
  void testComplexOperations() {
    PersistentBinaryTree<Integer> tree = emptyTree;

    // Вставляем в случайном порядке
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(50);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(25);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(75);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(10);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(30);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(60);
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(90);

    assertEquals(7, tree.size());
    assertTrue(tree.isBalanced());

    // Проверяем min и max
    assertEquals(Integer.valueOf(10), tree.min());
    assertEquals(Integer.valueOf(90), tree.max());

    // Удаляем элементы
    tree = (PersistentBinaryTree<Integer>) tree.createWithRemoved(25);
    tree = (PersistentBinaryTree<Integer>) tree.createWithRemoved(90);

    assertEquals(5, tree.size());
    assertTrue(tree.isBalanced());
    assertFalse(tree.containsElement(25));
    assertFalse(tree.containsElement(90));

    // Проверяем итератор (должен возвращать отсортированные элементы)
    List<Integer> elements = new ArrayList<>();
    for (Integer element : tree) {
      elements.add(element);
    }
    Collections.sort(elements);
    assertEquals(elements, new ArrayList<>(tree));
  }

  @Test
  @DisplayName("Tree remains balanced after many insertions and deletions")
  void testBalanceAfterManyOperations() {
    PersistentBinaryTree<Integer> tree = emptyTree;

    // Вставляем 100 элементов
    for (int i = 0; i < 100; i++) {
      tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(i);
    }

    // Дерево должно быть сбалансированным
    assertTrue(tree.isBalanced());
    assertEquals(100, tree.size());

    // Удаляем каждый второй элемент
    for (int i = 0; i < 100; i += 2) {
      tree = (PersistentBinaryTree<Integer>) tree.createWithRemoved(i);
    }

    // Дерево все еще должно быть сбалансированным
    assertTrue(tree.isBalanced());
    assertEquals(50, tree.size());

    // Проверяем min и max
    assertEquals(Integer.valueOf(1), tree.min());
    assertEquals(Integer.valueOf(99), tree.max());
  }

  @Test
  @DisplayName("Tree handles null elements correctly")
  void testNullElements() {
    PersistentBinaryTree<Integer> tree = emptyTree;

    // Попытка вставить null должна вернуть то же дерево
    tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(null);
    assertTrue(tree.isEmpty());

    // Проверка contains для null
    assertFalse(tree.containsElement(null));
    assertFalse(tree.contains(null));

    // Удаление null должно вернуть то же дерево
    tree = (PersistentBinaryTree<Integer>) balancedTree.createWithRemoved(null);
    assertSame(balancedTree, tree);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 5, 10, 100})
  @DisplayName("Add multiple elements and iterate in sorted order")
  void testAddMultipleElements(final int count) {
    PersistentBinaryTree<Integer> tree = emptyTree;
    List<Integer> expected = new ArrayList<>();

    // Вставляем в обратном порядке, чтобы проверить балансировку
    for (int i = count - 1; i >= 0; i--) {
      tree = (PersistentBinaryTree<Integer>) tree.createWithAdded(i);
      expected.add(i);
    }

    Collections.sort(expected);
    assertEquals(count, tree.size());

    List<Integer> actual = new ArrayList<>();
    for (Integer element : tree) {
      actual.add(element);
    }

    assertEquals(expected, actual);
    assertTrue(tree.isBalanced());
  }

  @Test
  @DisplayName("Tree rotation operations maintain correctness")
  void testTreeRotations() {
    // Создаем ситуацию, требующую левого поворота
    PersistentBinaryTree<Integer> tree =
        (PersistentBinaryTree<Integer>)
            emptyTree
                .createWithAdded(1)
                .createWithAdded(2)
                .createWithAdded(3); // Вырожденное правое дерево

    // После вставок дерево должно быть сбалансировано
    assertTrue(tree.isBalanced());
    assertEquals(3, tree.size());

    // Проверяем порядок элементов
    List<Integer> elements = new ArrayList<>();
    for (Integer element : tree) {
      elements.add(element);
    }
    assertEquals(Arrays.asList(1, 2, 3), elements);

    // Создаем ситуацию, требующую правого поворота
    tree =
        (PersistentBinaryTree<Integer>)
            emptyTree
                .createWithAdded(3)
                .createWithAdded(2)
                .createWithAdded(1); // Вырожденное левое дерево

    assertTrue(tree.isBalanced());
    assertEquals(3, tree.size());

    elements.clear();
    for (Integer element : tree) {
      elements.add(element);
    }
    assertEquals(Arrays.asList(1, 2, 3), elements);
  }
}
