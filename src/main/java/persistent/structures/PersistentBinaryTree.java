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
    
    @Override
    public String toString() {
        return "PersistentBinaryTree" + elements.toString();
    }
}