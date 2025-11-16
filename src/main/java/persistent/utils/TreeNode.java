package persistent.utils;

/**
 * A node for binary tree implementations of persistent structures.
 * This class represents a node in a balanced binary search tree with
 * height information for maintaining tree balance.
 * 
 * @param <K> the type of keys maintained by this tree node, must be Comparable
 * @param <V> the type of mapped values
 * @version 1.0
 */
public class TreeNode<K extends Comparable<K>, V> {
    /**
     * The key of this tree node.
     */
    private final K key;
    
    /**
     * The value associated with the key.
     */
    private final V value;
    
    /**
     * The left child of this node.
     */
    private final TreeNode<K, V> left;
    
    /**
     * The right child of this node.
     */
    private final TreeNode<K, V> right;
    
    /**
     * The height of the subtree rooted at this node.
     */
    private final int height;
    
    /**
     * Constructs a new tree node with the specified key, value, and children.
     * The height is automatically calculated based on the children's heights.
     * 
     * @param key the key for this node
     * @param value the value for this node
     * @param left the left child node
     * @param right the right child node
     */
    public TreeNode(K key, V value, TreeNode<K, V> left, TreeNode<K, V> right) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        this.height = 1 + Math.max(height(left), height(right));
    }
    
    /**
     * Returns the key of this tree node.
     * 
     * @return the key of this node
     */
    public K getKey() {
        return key;
    }
    
    /**
     * Returns the value associated with this tree node.
     * 
     * @return the value of this node
     */
    public V getValue() {
        return value;
    }
    
    /**
     * Returns the left child of this tree node.
     * 
     * @return the left child node, or null if no left child exists
     */
    public TreeNode<K, V> getLeft() {
        return left;
    }
    
    /**
     * Returns the right child of this tree node.
     * 
     * @return the right child node, or null if no right child exists
     */
    public TreeNode<K, V> getRight() {
        return right;
    }
    
    /**
     * Returns the height of the subtree rooted at this node.
     * 
     * @return the height of this node
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Calculates the height of the given tree node.
     * 
     * @param node the node to calculate height for, may be null
     * @return the height of the node, or 0 if the node is null
     */
    public static int height(TreeNode<?, ?> node) {
        return node == null ? 0 : node.getHeight();
    }
}