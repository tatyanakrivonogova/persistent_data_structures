package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent trees that provides mutable Collection
 * interface. Implements the 6-step transaction process.
 *
 * @param <T> the type of elements, must be Comparable
 */
public final class TransactionalPersistentBinaryTree<T extends Comparable<T>>
    implements Collection<T> {

  /** Atomic reference to the current version of the tree. */
  private final AtomicReference<PersistentBinaryTree<T>> currentRef;

  /** Creates a new empty transactional tree. */
  public TransactionalPersistentBinaryTree() {
    this.currentRef = new AtomicReference<>(new PersistentBinaryTree<>());
  }

  /**
   * Creates a transactional tree with the given initial state.
   *
   * @param initial the initial tree state
   */
  public TransactionalPersistentBinaryTree(
      final PersistentBinaryTree<T> initial) {
    this.currentRef = new AtomicReference<>(initial);
  }

  /**
   * Executes a modification operation atomically.
   *
   * @param operation the function to apply to the tree
   * @return true if the operation succeeded and changed the tree
   */
  private boolean modify(
      final java.util.function.Function<PersistentBinaryTree<T>,
      PersistentBinaryTree<T>> operation) {
    while (true) {
      PersistentBinaryTree<T> current = currentRef.get();
      PersistentBinaryTree<T> newVersion = operation.apply(current);

      if (newVersion == current) {
        return false; // No changes
      }

      if (currentRef.compareAndSet(current, newVersion)) {
        return true; // Success
      }
      // Retry on conflict
    }
  }

  @Override
  public boolean add(final T e) {
    return modify(tree -> (PersistentBinaryTree<T>) tree.createWithAdded(e));
  }

  @Override
  public boolean remove(final Object o) {
    try {
      @SuppressWarnings("unchecked")
      T element = (T) o;
      return modify(
          tree -> (PersistentBinaryTree<T>) tree.createWithRemoved(element));
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public boolean contains(final Object o) {
    return currentRef.get().contains(o);
  }

  @Override
  public int size() {
    return currentRef.get().size();
  }

  @Override
  public boolean isEmpty() {
    return currentRef.get().isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return currentRef.get().iterator();
  }

  @Override
  public Object[] toArray() {
    return currentRef.get().toArray();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E> E[] toArray(final E[] a) {
    return currentRef.get().toArray(a);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return currentRef.get().containsAll(c);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    if (c.isEmpty()) {
      return false;
    }
    return modify(
        tree -> {
          PersistentBinaryTree<T> result = tree;
          for (T element : c) {
            result = (PersistentBinaryTree<T>) result.createWithAdded(element);
          }
          return result;
        });
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    if (c.isEmpty()) {
      return false;
    }
    return modify(
        tree -> {
          PersistentBinaryTree<T> result = tree;
          boolean changed = false;
          for (Object obj : c) {
            try {
              @SuppressWarnings("unchecked")
              T element = (T) obj;
              PersistentBinaryTree<T> newResult =
                  (PersistentBinaryTree<T>) result.createWithRemoved(element);
              if (newResult != result) {
                changed = true;
                result = newResult;
              }
            } catch (ClassCastException e) {
              // Skip elements of wrong type
            }
          }
          return changed ? result : tree;
        });
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    if (c.isEmpty()) {
      boolean wasEmpty = isEmpty();
      if (wasEmpty) {
        return false;
      }
      modify(tree -> new PersistentBinaryTree<>());
      return true;
    }

    return modify(
        tree -> {
          PersistentBinaryTree<T> newTree = new PersistentBinaryTree<>();
          boolean changed = false;

          for (T element : tree) {
            if (c.contains(element)) {
              newTree = (PersistentBinaryTree<T>) newTree.createWithAdded(
                  element);
            } else {
              changed = true;
            }
          }

          // Check if the new tree is different from the old one
          if (!changed && tree.size() == newTree.size()) {
            // Check if trees are actually equal
            boolean treesEqual = true;
            Iterator<T> it1 = tree.iterator();
            Iterator<T> it2 = newTree.iterator();
            while (it1.hasNext() && it2.hasNext()) {
              if (!it1.next().equals(it2.next())) {
                treesEqual = false;
                break;
              }
            }
            if (treesEqual && !it1.hasNext() && !it2.hasNext()) {
              return tree; // No changes
            }
          }

          return newTree;
        });
  }

  @Override
  public void clear() {
    modify(tree -> new PersistentBinaryTree<>());
  }

  /**
   * Returns the current immutable snapshot.
   *
   * @return the current tree snapshot
   */
  public PersistentBinaryTree<T> snapshot() {
    return currentRef.get();
  }

  /**
   * Returns a transactional wrapper for the current snapshot.
   *
   * @return a new transactional wrapper for the current tree
   */
  public TransactionalPersistentBinaryTree<T> transactionalCopy() {
    return new TransactionalPersistentBinaryTree<>(currentRef.get());
  }

  /**
   * Returns the minimum element in this tree.
   *
   * @return the minimum element
   * @throws java.util.NoSuchElementException if the tree is empty
   */
  public T min() {
    return currentRef.get().min();
  }

  /**
   * Returns the maximum element in this tree.
   *
   * @return the maximum element
   * @throws java.util.NoSuchElementException if the tree is empty
   */
  public T max() {
    return currentRef.get().max();
  }

  /**
   * Returns the height of this tree.
   *
   * @return the height of the tree
   */
  public int height() {
    return currentRef.get().height();
  }

  /**
   * Checks if this tree is balanced.
   *
   * @return true if the tree is balanced, false otherwise
   */
  public boolean isBalanced() {
    return currentRef.get().isBalanced();
  }

  @Override
  public String toString() {
    return currentRef.get().toString();
  }
}