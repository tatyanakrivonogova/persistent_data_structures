package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional wrapper for persistent trees that provides mutable Collection interface. Implements
 * the 6-step transaction process.
 */
public class TransactionalPersistentBinaryTree<T extends Comparable<T>> implements Collection<T> {

  private final AtomicReference<PersistentBinaryTree<T>> currentRef;

  public TransactionalPersistentBinaryTree() {
    this.currentRef = new AtomicReference<>(new PersistentBinaryTree<>());
  }

  public TransactionalPersistentBinaryTree(PersistentBinaryTree<T> initial) {
    this.currentRef = new AtomicReference<>(initial);
  }

  /** Executes a modification operation atomically. */
  private boolean modify(
      java.util.function.Function<PersistentBinaryTree<T>, PersistentBinaryTree<T>> operation) {
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
  public boolean add(T e) {
    return modify(tree -> (PersistentBinaryTree<T>) tree.createWithAdded(e));
  }

  @Override
  public boolean remove(Object o) {
    try {
      @SuppressWarnings("unchecked")
      T element = (T) o;
      return modify(tree -> (PersistentBinaryTree<T>) tree.createWithRemoved(element));
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public boolean contains(Object o) {
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
  public <E> E[] toArray(E[] a) {
    return currentRef.get().toArray(a);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return currentRef.get().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
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
  public boolean removeAll(Collection<?> c) {
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
  public boolean retainAll(Collection<?> c) {
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
              newTree = (PersistentBinaryTree<T>) newTree.createWithAdded(element);
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

  /** Returns the current immutable snapshot. */
  public PersistentBinaryTree<T> snapshot() {
    return currentRef.get();
  }

  /** Returns a transactional wrapper for the current snapshot. */
  public TransactionalPersistentBinaryTree<T> transactionalCopy() {
    return new TransactionalPersistentBinaryTree<>(currentRef.get());
  }

  // Tree-specific methods
  public T min() {
    return currentRef.get().min();
  }

  public T max() {
    return currentRef.get().max();
  }

  public int height() {
    return currentRef.get().height();
  }

  public boolean isBalanced() {
    return currentRef.get().isBalanced();
  }

  @Override
  public String toString() {
    return currentRef.get().toString();
  }
}
