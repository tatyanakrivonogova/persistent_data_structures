package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.utils.BinaryTreeNode;

import java.util.*;
import java.util.function.Consumer;

/**
 * A persistent binary search tree implementation.
 * Provides efficient storage and retrieval of comparable elements with AVL balancing.
 * 
 * @param <T> the type of elements in this tree, must be Comparable
 * @version 1.0
 */
public class PersistentBinaryTree<T extends Comparable<T>> extends AbstractPersistentStructure<T> {
    private final BinaryTreeNode<T> root;
    private final int size;
    
    /**
     * Constructs an empty persistent binary tree.
     */
    public PersistentBinaryTree() {
        super();
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Private constructor for creating new versions of the tree.
     */
    private PersistentBinaryTree(BinaryTreeNode<T> root, int size, Version version) {
        super(version);
        this.root = root;
        this.size = size;
    }
    
    /**
     * Inserts the specified element into this tree.
     * 
     * @param value the element to be inserted
     * @return a new persistent tree containing the specified element
     */
    public PersistentBinaryTree<T> insert(T value) {
        BinaryTreeNode<T> newRoot = insert(root, value);
        boolean valueExists = newRoot == root && root != null && contains(value);
        
        Version newVersion = createNewVersion();
        return new PersistentBinaryTree<>(
            newRoot,
            valueExists ? size : size + 1,
            newVersion
        );
    }
    
    /**
     * Removes the specified element from this tree if present.
     * 
     * @param value the element to be removed
     * @return a new persistent tree without the specified element
     */
    public PersistentBinaryTree<T> remove(T value) {
        BinaryTreeNode<T> newRoot = remove(root, value);
        if (newRoot == root) {
            return this; // Value not found
        }
        
        Version newVersion = createNewVersion();
        return new PersistentBinaryTree<>(newRoot, size - 1, newVersion);
    }
    
    /**
     * Returns true if this tree contains the specified element.
     * 
     * @param value the element whose presence in this tree is to be tested
     * @return true if this tree contains the specified element
     */
    public boolean contains(T value) {
        return contains(root, value);
    }
    
    /**
     * Returns the minimum element in this tree.
     * 
     * @return the minimum element in this tree
     * @throws NoSuchElementException if this tree is empty
     */
    public T min() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMin(root).getValue();
    }
    
    /**
     * Returns the maximum element in this tree.
     * 
     * @return the maximum element in this tree
     * @throws NoSuchElementException if this tree is empty
     */
    public T max() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMax(root).getValue();
    }
    
    /**
     * Performs an in-order traversal of the tree.
     * 
     * @param action the action to be performed for each element
     */
    public void inOrderTraversal(Consumer<T> action) {
        inOrderTraversal(root, action);
    }
    
    /**
     * Performs a pre-order traversal of the tree.
     * 
     * @param action the action to be performed for each element
     */
    public void preOrderTraversal(Consumer<T> action) {
        preOrderTraversal(root, action);
    }
    
    /**
     * Performs a post-order traversal of the tree.
     * 
     * @param action the action to be performed for each element
     */
    public void postOrderTraversal(Consumer<T> action) {
        postOrderTraversal(root, action);
    }
    
    /**
     * Returns the height of the tree.
     * The height of an empty tree is 0.
     * 
     * @return the height of the tree
     */
    public int height() {
        return root == null ? 0 : root.getHeight();
    }
    
    /**
     * Returns true if the tree is balanced (AVL property is maintained).
     * 
     * @return true if the tree is balanced
     */
    public boolean isBalanced() {
        return isBalanced(root);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }
    
    /**
     * {@inheritDoc}
     * Returns an iterator over elements in in-order traversal order.
     */
    @Override
    public Iterator<T> iterator() {
        return inOrderList().iterator();
    }
    
    private BinaryTreeNode<T> insert(BinaryTreeNode<T> node, T value) {
        if (node == null) {
            return new BinaryTreeNode<>(value, null, null);
        }
        
        int cmp = value.compareTo(node.getValue());
        if (cmp < 0) {
            BinaryTreeNode<T> newLeft = insert(node.getLeft(), value);
            return balance(new BinaryTreeNode<>(
                node.getValue(),
                newLeft,
                node.getRight()
            ));
        } else if (cmp > 0) {
            BinaryTreeNode<T> newRight = insert(node.getRight(), value);
            return balance(new BinaryTreeNode<>(
                node.getValue(),
                node.getLeft(),
                newRight
            ));
        } else {
            // Value already exists, return unchanged node
            return node;
        }
    }
    
    private BinaryTreeNode<T> remove(BinaryTreeNode<T> node, T value) {
        if (node == null) {
            return null;
        }
        
        int cmp = value.compareTo(node.getValue());
        if (cmp < 0) {
            BinaryTreeNode<T> newLeft = remove(node.getLeft(), value);
            return balance(new BinaryTreeNode<>(
                node.getValue(),
                newLeft,
                node.getRight()
            ));
        } else if (cmp > 0) {
            BinaryTreeNode<T> newRight = remove(node.getRight(), value);
            return balance(new BinaryTreeNode<>(
                node.getValue(),
                node.getLeft(),
                newRight
            ));
        } else {
            // Node to be removed found
            if (node.getLeft() == null) {
                return node.getRight();
            }
            if (node.getRight() == null) {
                return node.getLeft();
            }
            
            // Node with two children
            BinaryTreeNode<T> minNode = findMin(node.getRight());
            return balance(new BinaryTreeNode<>(
                minNode.getValue(),
                node.getLeft(),
                removeMin(node.getRight())
            ));
        }
    }
    
    private boolean contains(BinaryTreeNode<T> node, T value) {
        if (node == null) {
            return false;
        }
        
        int cmp = value.compareTo(node.getValue());
        if (cmp < 0) {
            return contains(node.getLeft(), value);
        } else if (cmp > 0) {
            return contains(node.getRight(), value);
        } else {
            return true;
        }
    }
    
    private BinaryTreeNode<T> findMin(BinaryTreeNode<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }
    
    private BinaryTreeNode<T> findMax(BinaryTreeNode<T> node) {
        while (node.getRight() != null) {
            node = node.getRight();
        }
        return node;
    }
    
    private BinaryTreeNode<T> removeMin(BinaryTreeNode<T> node) {
        if (node.getLeft() == null) {
            return node.getRight();
        }
        BinaryTreeNode<T> newLeft = removeMin(node.getLeft());
        return balance(new BinaryTreeNode<>(
            node.getValue(),
            newLeft,
            node.getRight()
        ));
    }
    
    private BinaryTreeNode<T> balance(BinaryTreeNode<T> node) {
        if (node == null) {
            return null;
        }
        
        int balanceFactor = node.getBalanceFactor();
        
        // Left heavy
        if (balanceFactor > 1) {
            if (node.getLeft().getBalanceFactor() < 0) {
                // Left-Right case
                return rotateRight(new BinaryTreeNode<>(
                    node.getValue(),
                    rotateLeft(node.getLeft()),
                    node.getRight()
                ));
            }
            // Left-Left case
            return rotateRight(node);
        }
        
        // Right heavy
        if (balanceFactor < -1) {
            if (node.getRight().getBalanceFactor() > 0) {
                // Right-Left case
                return rotateLeft(new BinaryTreeNode<>(
                    node.getValue(),
                    node.getLeft(),
                    rotateRight(node.getRight())
                ));
            }
            // Right-Right case
            return rotateLeft(node);
        }
        
        return node;
    }
    
    private BinaryTreeNode<T> rotateRight(BinaryTreeNode<T> y) {
        BinaryTreeNode<T> x = y.getLeft();
        BinaryTreeNode<T> T2 = x.getRight();
        
        return new BinaryTreeNode<>(
            x.getValue(),
            x.getLeft(),
            new BinaryTreeNode<>(y.getValue(), T2, y.getRight())
        );
    }
    
    private BinaryTreeNode<T> rotateLeft(BinaryTreeNode<T> x) {
        BinaryTreeNode<T> y = x.getRight();
        BinaryTreeNode<T> T2 = y.getLeft();
        
        return new BinaryTreeNode<>(
            y.getValue(),
            new BinaryTreeNode<>(x.getValue(), x.getLeft(), T2),
            y.getRight()
        );
    }
    
    private boolean isBalanced(BinaryTreeNode<T> node) {
        if (node == null) {
            return true;
        }
        
        int balanceFactor = node.getBalanceFactor();
        if (Math.abs(balanceFactor) > 1) {
            return false;
        }
        
        return isBalanced(node.getLeft()) && isBalanced(node.getRight());
    }
    
    private void inOrderTraversal(BinaryTreeNode<T> node, Consumer<T> action) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), action);
            action.accept(node.getValue());
            inOrderTraversal(node.getRight(), action);
        }
    }
    
    private void preOrderTraversal(BinaryTreeNode<T> node, Consumer<T> action) {
        if (node != null) {
            action.accept(node.getValue());
            preOrderTraversal(node.getLeft(), action);
            preOrderTraversal(node.getRight(), action);
        }
    }
    
    private void postOrderTraversal(BinaryTreeNode<T> node, Consumer<T> action) {
        if (node != null) {
            postOrderTraversal(node.getLeft(), action);
            postOrderTraversal(node.getRight(), action);
            action.accept(node.getValue());
        }
    }
    
    private List<T> inOrderList() {
        List<T> result = new ArrayList<>();
        inOrderTraversal(root, result::add);
        return result;
    }
    
    @Override
    public String toString() {
        List<String> elements = new ArrayList<>();
        inOrderTraversal(value -> elements.add(value.toString()));
        return "PersistentBinaryTree[" + String.join(", ", elements) + "]";
    }
}