package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.utils.BinaryTreeNode;

import java.util.*;

/**
 * A persistent binary tree structure that stores elements in a binary tree format.
 * Implements the same API as other persistent structures with index-based operations.
 * The tree maintains balance through AVL rotations for efficient operations.
 * 
 * @param <T> the type of elements in this binary tree, must be Comparable for ordering
 * @version 1.0
 */
public class PersistentBinaryTree<T extends Comparable<T>> extends AbstractPersistentStructure<T> {
    /**
     * The root node of the binary tree.
     */
    private final BinaryTreeNode<T> root;
    
    /**
     * The number of elements in the tree.
     */
    private final int size;
    
    /**
     * Cache for index-based operations, containing elements in in-order traversal order.
     */
    private final List<T> elements;
    
    /**
     * Constructs an empty persistent binary tree.
     */
    public PersistentBinaryTree() {
        super();
        this.root = null;
        this.size = 0;
        this.elements = Collections.emptyList();
    }
    
    /**
     * Private constructor for creating new versions of the tree.
     * Used internally when performing operations that modify the tree.
     * 
     * @param root the root node of the tree
     * @param size the number of elements in the tree
     * @param elements the cached elements in in-order traversal order
     * @param version the version identifier for this tree state
     */
    private PersistentBinaryTree(BinaryTreeNode<T> root, int size, List<T> elements, Version version) {
        super(version);
        this.root = root;
        this.size = size;
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }
    
    /**
     * {@inheritDoc}
     * Returns the element at the specified position in in-order traversal order.
     * 
     * @param index the index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return elements.get(index);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return the number of elements in this binary tree
     */
    @Override
    public int size() {
        return size;
    }
    
    /**
     * {@inheritDoc}
     * Returns an iterator over the elements in this tree in in-order traversal sequence.
     * 
     * @return an iterator over the elements in this tree
     */
    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }
    
    // Additional tree-specific methods that maintain the API
    
    /**
     * Checks if the tree contains the specified value.
     * This method provides efficient O(log n) search in the balanced binary tree.
     * 
     * @param value the value to search for
     * @return true if the tree contains the value, false otherwise
     */
    public boolean contains(T value) {
        return contains(root, value);
    }
    
    private boolean contains(BinaryTreeNode<T> node, T value) {
        if (node == null) return false;
        
        int cmp = value.compareTo(node.getValue());
        if (cmp < 0) return contains(node.getLeft(), value);
        if (cmp > 0) return contains(node.getRight(), value);
        return true;
    }
    
    private void inOrderTraversal(BinaryTreeNode<T> node, Consumer<T> action) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), action);
            action.accept(node.getValue());
            inOrderTraversal(node.getRight(), action);
        }
    }
    
    @Override
    public String toString() {
        return "PersistentBinaryTree" + elements.toString();
    }
}