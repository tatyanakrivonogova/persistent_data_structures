package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.core.TransactionalVersion;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * A persistent binary search tree implementation with transaction support.
 *
 * @param <T> the type of elements in this tree, must be Comparable
 * @version 3.0
 */
public class PersistentBinaryTree<T extends Comparable<T>>
        extends AbstractPersistentStructure<T> {
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
    private static class BinaryTreeNode<T extends Comparable<T>> {
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
         * Constructs a new binary tree node with the specified
         * value and children.
         * The height is automatically calculated based on the
         * children's heights.
         *
         * @param valueValue the value to store in this node
         * @param leftValue the left child node, may be null
         * @param rightValue the right child node, may be null
         */
        BinaryTreeNode(final T valueValue,
            final BinaryTreeNode<T> leftValue,
            final BinaryTreeNode<T> rightValue) {
            this.value = valueValue;
            this.left = leftValue;
            this.right = rightValue;
            this.height = 1 + Math.max(height(leftValue), height(rightValue));
        }

        /**
         * Calculates the height of the given tree node.
         *
         * @param <T> the type of elements stored in the binary node
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
            return right != null ? right.equals(that.right)
                : that.right == null;
        }

        /**
         * Returns a hash code value for this node based on its
         * value and structure.
         *
         * @return hash code value for this node
         */
        @Override
        public int hashCode() {
            int result = value.hashCode();
            final int mul = 31;
            result = mul * result + (left != null ? left.hashCode() : 0);
            result = mul * result + (right != null ? right.hashCode() : 0);
            result = mul * result + height;
            return result;
        }
    }

    /**
     * The root node of the binary tree.
     */
    private BinaryTreeNode<T> root;

    /**
     * The number of elements in the binary tree.
     */
    private int size;

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
     * Creates a new persistent binary tree instance with specified
     * root node, size, and version.
     * Used internally for creating modified versions without
     * altering the original tree.
     *
     * @param rootValue the root node of the tree,
     * can be null for empty tree
     * @param sizeValue the number of elements in the tree
     * @param version   the version identifier for this tree instance
     */
    private PersistentBinaryTree(final BinaryTreeNode<T> rootValue,
        final int sizeValue, final Version version) {
        super(version);
        this.root = rootValue;
        this.size = sizeValue;
    }

    /**
     * Creates a deep copy of the current tree.
     * Creates a new persistent binary tree instance that shares the structure
     * but has independent version tracking. Useful for creating new versions
     * while preserving immutability.
     *
     * @return a new PersistentBinaryTree instance with the same
     * structure and size but separate version identity
     */
    private PersistentBinaryTree<T> deepCopy() {
        return new PersistentBinaryTree<>(this.root, this.size,
            this.getVersion());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void savePreTransactionState() {
        this.preTransactionState = this.deepCopy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void restoreFromPreTransactionState() {
        if (preTransactionState instanceof PersistentBinaryTree) {
            PersistentBinaryTree<T> savedState =
                (PersistentBinaryTree<T>) preTransactionState;
            this.root = savedState.root;
            this.size = savedState.size;
            this.setVersion(savedState.getVersion());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void convertToFinalVersion() {
        // Convert transactional version to final version
        Version current = getVersion();
        if (current instanceof TransactionalVersion) {
            TransactionalVersion tv = (TransactionalVersion) current;
            if (tv.isTransactional()) {
                setVersion(new TransactionalVersion(tv.getId(),
                    null, false, 0));
            }
        }
    }

    /**
     * Inserts the specified element into this tree.
     * Transaction-aware: in transaction, modifications are isolated.
     *
     * @param value the element to be inserted
     * @return a new persistent tree containing the specified element
     */
    public PersistentBinaryTree<T> insert(final T value) {
        BinaryTreeNode<T> newRoot = insert(root, value);
        boolean valueExists = newRoot == root && root != null
            && contains(value);

        Version newVersion = createNewVersion();
        PersistentBinaryTree<T> result = new PersistentBinaryTree<>(
            newRoot,
            valueExists ? size : size + 1,
            newVersion
        );

        // If in transaction, update current instance
        if (isInTransaction()) {
            this.root = result.root;
            this.size = result.size;
            this.setVersion(newVersion);
            return this;
        }

        return result;
    }

    /**
     * Removes the specified element from this tree if present.
     * Transaction-aware: in transaction, modifications are isolated.
     *
     * @param value the element to be removed
     * @return a new persistent tree without the specified element
     */
    public PersistentBinaryTree<T> remove(final T value) {
        BinaryTreeNode<T> newRoot = remove(root, value);
        if (newRoot == root) {
            return this; // Value not found
        }

        Version newVersion = createNewVersion();
        PersistentBinaryTree<T> result = new PersistentBinaryTree<>(
            newRoot,
            size - 1,
            newVersion
        );

        // If in transaction, update current instance
        if (isInTransaction()) {
            this.root = result.root;
            this.size = result.size;
            this.setVersion(newVersion);
            return this;
        }

        return result;
    }

    /**
     * Returns true if this tree contains the specified element.
     *
     * @param value the element whose presence in this tree is to be tested
     * @return true if this tree contains the specified element
     */
    public boolean contains(final T value) {
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
    public void inOrderTraversal(final Consumer<T> action) {
        inOrderTraversal(root, action);
    }

    /**
     * Performs a pre-order traversal of the tree.
     *
     * @param action the action to be performed for each element
     */
    public void preOrderTraversal(final Consumer<T> action) {
        preOrderTraversal(root, action);
    }

    /**
     * Performs a post-order traversal of the tree.
     *
     * @param action the action to be performed for each element
     */
    public void postOrderTraversal(final Consumer<T> action) {
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

    private BinaryTreeNode<T> insert(final BinaryTreeNode<T> node,
        final T value) {
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

    private BinaryTreeNode<T> remove(final BinaryTreeNode<T> node,
        final T value) {
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

    private boolean contains(final BinaryTreeNode<T> node, final T value) {
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

    private BinaryTreeNode<T> findMin(final BinaryTreeNode<T> node) {
        BinaryTreeNode<T> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    private BinaryTreeNode<T> findMax(final BinaryTreeNode<T> node) {
        BinaryTreeNode<T> current = node;
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current;
    }

    private BinaryTreeNode<T> removeMin(final BinaryTreeNode<T> node) {
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

    private BinaryTreeNode<T> balance(final BinaryTreeNode<T> node) {
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

    private BinaryTreeNode<T> rotateRight(final BinaryTreeNode<T> y) {
        BinaryTreeNode<T> x = y.getLeft();
        BinaryTreeNode<T> t2 = x.getRight();

        return new BinaryTreeNode<>(
            x.getValue(),
            x.getLeft(),
            new BinaryTreeNode<>(y.getValue(), t2, y.getRight())
        );
    }

    private BinaryTreeNode<T> rotateLeft(final BinaryTreeNode<T> x) {
        BinaryTreeNode<T> y = x.getRight();
        BinaryTreeNode<T> t2 = y.getLeft();

        return new BinaryTreeNode<>(
            y.getValue(),
            new BinaryTreeNode<>(x.getValue(), x.getLeft(), t2),
            y.getRight()
        );
    }

    private boolean isBalanced(final BinaryTreeNode<T> node) {
        if (node == null) {
            return true;
        }

        int balanceFactor = node.getBalanceFactor();
        if (Math.abs(balanceFactor) > 1) {
            return false;
        }

        return isBalanced(node.getLeft()) && isBalanced(node.getRight());
    }

    private void inOrderTraversal(final BinaryTreeNode<T> node,
        final Consumer<T> action) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), action);
            action.accept(node.getValue());
            inOrderTraversal(node.getRight(), action);
        }
    }

    private void preOrderTraversal(final BinaryTreeNode<T> node,
        final Consumer<T> action) {
        if (node != null) {
            action.accept(node.getValue());
            preOrderTraversal(node.getLeft(), action);
            preOrderTraversal(node.getRight(), action);
        }
    }

    private void postOrderTraversal(final BinaryTreeNode<T> node,
        final Consumer<T> action) {
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

    /**
     * Returns a snapshot of the current tree state.
     * Useful for getting a consistent view during transactions.
     * Creates an independent copy of the tree that won't be affected
     * by subsequent modifications to the original.
     *
     * @return a new PersistentBinaryTree instance representing
     * the current state as an immutable snapshot
     */
    public PersistentBinaryTree<T> snapshot() {
        return this.deepCopy();
    }

    @Override
    public final String toString() {
        List<String> elements = new ArrayList<>();
        inOrderTraversal(value -> elements.add(value.toString()));
        return "PersistentBinaryTree[" + String.join(", ", elements) + "]";
    }
}
