package persistent.utils;

/**
 * A node for binary tree implementations of persistent structures.
 * This class represents a node in a balanced binary search tree with
 * height information for maintaining tree balance. The node is immutable
 * to ensure thread safety and persistence.
 *
 * @param <T> the type of values stored in the node,
 * must be Comparable for ordering
 * @version 1.0
 */
public class BinaryTreeNode<T extends Comparable<T>> {
    /**
     * The value stored in this node.
     */
    private final T value;

    /**
     * The left child of this node.
     */
    private final BinaryTreeNode<T> left;

    /**
     * The right child of this node.
     */
    private final BinaryTreeNode<T> right;

    /**
     * The height of the subtree rooted at this node.
     */
    private final int height;

    /**
     * Constructs a new binary tree node with the specified value and children.
     * The height is automatically calculated based on the children's heights.
     *
     * @param value the value to store in this node
     * @param left the left child node, may be null
     * @param right the right child node, may be null
     */
    public BinaryTreeNode(final T valueValue, final BinaryTreeNode<T> leftValue, 
        final BinaryTreeNode<T> rightValue) {
        this.value = valueValue;
        this.left = leftValue;
        this.right = rightValue;
        this.height = 1 + Math.max(height(leftValue), height(rightValue));
    }

    /**
     * Calculates the height of the given tree node.
     *
     * @param node the node to calculate height for, may be null
     * @return the height of the node, or 0 if the node is null
     */
    private static <T extends Comparable<T>> int 
        height(final BinaryTreeNode<T> node) {
        return node == null ? 0 : node.getHeight();
    }

    /**
     * Returns the value stored in this node.
     *
     * @return the value of this node
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns the left child of this node.
     *
     * @return the left child node, or null if no left child exists
     */
    public BinaryTreeNode<T> getLeft() {
        return left;
    }

    /**
     * Returns the right child of this node.
     *
     * @return the right child node, or null if no right child exists
     */
    public BinaryTreeNode<T> getRight() {
        return right;
    }

    /**
     * Returns the height of the subtree rooted at this node.
     * The height of a node is the number of edges on the longest path
     * from the node to a leaf. A leaf node has height 1.
     *
     * @return the height of this node
     */
    public int getHeight() {
        return height;
    }

    /**
     * Creates a new node with the same structure but different value.
     *
     * @param newValue the new value for the node
     * @return a new node with the updated value and same children
     */
    public BinaryTreeNode<T> withValue(final T newValue) {
        return new BinaryTreeNode<>(newValue, this.left, this.right);
    }

    /**
     * Creates a new node with the same value but different left child.
     *
     * @param newLeft the new left child for the node
     * @return a new node with the updated left child and same value/right child
     */
    public BinaryTreeNode<T> withLeft(final BinaryTreeNode<T> newLeft) {
        return new BinaryTreeNode<>(this.value, newLeft, this.right);
    }

    /**
     * Creates a new node with the same value but different right child.
     *
     * @param newRight the new right child for the node
     * @return a new node with the updated right child and same value/left child
     */
    public BinaryTreeNode<T> withRight(final BinaryTreeNode<T> newRight) {
        return new BinaryTreeNode<>(this.value, this.left, newRight);
    }

    /**
     * Returns the balance factor of this node.
     * The balance factor is defined as the difference between the height
     * of the left subtree and the height of the right subtree.
     *
     * @return the balance factor (left height - right height)
     */
    public int getBalanceFactor() {
        return height(left) - height(right);
    }

    /**
     * Checks if this node is a leaf node (has no children).
     *
     * @return true if this node has no children, false otherwise
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * Checks if this node has exactly one child.
     *
     * @return true if this node has exactly one child, false otherwise
     */
    public boolean hasSingleChild() {
        return (left == null && right != null) 
            || (left != null && right == null);
    }

    /**
     * Returns a string representation of this node".
     *
     * @return string representation of this node
     */
    @Override
    public String toString() {
        return value + "[" + height + "]";
    }

    /**
     * Compares this node with another object for equality.
     * Two BinaryTreeNode objects are equal if they have the same value,
     * same left subtree, and same right subtree.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BinaryTreeNode<T> that = (BinaryTreeNode<T>) obj;

        if (height != that.height) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }
        if (left != null ? !left.equals(that.left) : that.left != null) {
            return false;
        }
        return right != null ? right.equals(that.right) : that.right == null;
    }

    /**
     * Returns a hash code value for this node based on its value and structure.
     *
     * @return hash code value for this node
     */
    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + height;
        return result;
    }
}
