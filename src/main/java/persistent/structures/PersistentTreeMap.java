package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.core.TransactionalVersion;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;

/**
 * A persistent associative array based on AVL tree with transaction support.
 *
 * @param <K> the type of keys maintained by this map, must be Comparable
 * @param <V> the type of mapped values
 * @version 3.0
 */
public class PersistentTreeMap<K extends Comparable<K>, V>
    extends AbstractPersistentStructure<Map.Entry<K, V>> {

    /**
     * A node for binary tree implementations of persistent structures.
     * This class represents a node in a balanced binary search tree with
     * height information for maintaining tree balance.
     *
     * @param <K> the type of keys maintained by this tree node,
     * must be Comparable
     * @param <V> the type of mapped values
     * @version 1.0
     */
    private static class TreeNode<K extends Comparable<K>, V> {
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
         * Constructs a new tree node with the specified
         * key, value, and children.
         * The height is automatically calculated based
         * on the children's heights.
         *
         * @param keyValue the key for this node
         * @param valueValue the value for this node
         * @param leftValue the left child node
         * @param rightValue the right child node
         */
        TreeNode(final K keyValue, final V valueValue,
            final TreeNode<K, V> leftValue, final TreeNode<K, V> rightValue) {
            this.key = keyValue;
            this.value = valueValue;
            this.left = leftValue;
            this.right = rightValue;
            this.height = 1 + Math.max(height(leftValue), height(rightValue));
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
        private static int height(final TreeNode<?, ?> node) {
            return node == null ? 0 : node.getHeight();
        }
    }

    /**
     * The root node of the tree.
     */
    private TreeNode<K, V> root;

    /**
     * The number of elements in the binary tree.
     */
    private int size;

    /**
     * Constructs an empty persistent tree map.
     */
    public PersistentTreeMap() {
        super();
        this.root = null;
        this.size = 0;
    }

    /**
     * Private constructor for creating new versions of the map.
     */
    private PersistentTreeMap(final TreeNode<K, V> rootValue,
                              final int sizeValue, final Version versionValue) {
        super(versionValue);
        this.root = rootValue;
        this.size = sizeValue;
    }

    /**
     * Creates a deep copy of the current map.
     */
    private PersistentTreeMap<K, V> deepCopy() {
        return new PersistentTreeMap<>(this.root, this.size, this.getVersion());
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
        if (preTransactionState instanceof PersistentTreeMap) {
            PersistentTreeMap<K, V> savedState =
                (PersistentTreeMap<K, V>) preTransactionState;
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
                setVersion(new TransactionalVersion(tv.getId(), null,
                    false, 0));
            }
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * Transaction-aware: in transaction, modifications are isolated.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return a new persistent map containing the specified key-value mapping
     */
    public PersistentTreeMap<K, V> put(final K key, final V value) {
        TreeNode<K, V> newRoot = put(root, key, value);
        boolean keyExists = get(root, key) != null;

        Version newVersion = createNewVersion();
        PersistentTreeMap<K, V> result = new PersistentTreeMap<>(
            newRoot,
            keyExists ? size : size + 1,
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
     * Removes the mapping for the specified key from this map if present.
     * Transaction-aware: in transaction, modifications are isolated.
     *
     * @param key the key whose mapping is to be removed from the map
     * @return a new persistent map without the specified key mapping
     */
    public PersistentTreeMap<K, V> remove(final K key) {
        if (!containsKey(key)) {
            return this; // Key not found - return same instance
        }

        TreeNode<K, V> newRoot = remove(root, key);
        Version newVersion = createNewVersion();
        PersistentTreeMap<K, V> result = new PersistentTreeMap<>(
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
     * Returns the value to which the specified key is mapped,
     * or null if no mapping exists.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped,
     * or null if not present
     */
    public V get(final K key) {
        TreeNode<K, V> node = get(root, key);
        return node == null ? null : node.getValue();
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    public boolean containsKey(final K key) {
        return get(key) != null;
    }

    /**
     * Returns true if this map contains the specified value.
     *
     * @param value the value whose presence in this map is to be tested
     * @return true if this map contains the specified value
     */
    public boolean containsValue(final V value) {
        return containsValue(root, value);
    }

    /**
     * Returns the first (lowest) key currently in this map.
     *
     * @return the first key in this map
     * @throws NoSuchElementException if this map is empty
     */
    public K firstKey() {
        if (root == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return findMin(root).getKey();
    }

    /**
     * Returns the last (highest) key currently in this map.
     *
     * @return the last key in this map
     * @throws NoSuchElementException if this map is empty
     */
    public K lastKey() {
        if (root == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return findMax(root).getKey();
    }

    /**
     * Returns the height of the underlying tree.
     *
     * @return the height of the tree
     */
    public int height() {
        return root == null ? 0 : root.getHeight();
    }

    /**
     * Returns true if the underlying tree is balanced.
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
     * Returns an iterator over entries in key-sorted order.
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return inOrderTraversal().iterator();
    }

    private TreeNode<K, V> put(final TreeNode<K, V> node,
        final K key, final V value) {
        if (node == null) {
            return new TreeNode<>(key, value, null, null);
        }

        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            TreeNode<K, V> newLeft = put(node.getLeft(), key, value);
            return balance(new TreeNode<>(
                node.getKey(), node.getValue(),
                newLeft,
                node.getRight()
            ));
        } else if (cmp > 0) {
            TreeNode<K, V> newRight = put(node.getRight(), key, value);
            return balance(new TreeNode<>(
                node.getKey(), node.getValue(),
                node.getLeft(),
                newRight
            ));
        } else {
            // Update existing key
            return new TreeNode<>(key, value, node.getLeft(), node.getRight());
        }
    }

    private TreeNode<K, V> remove(final TreeNode<K, V> node, final K key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            TreeNode<K, V> newLeft = remove(node.getLeft(), key);
            return balance(new TreeNode<>(
                node.getKey(), node.getValue(),
                newLeft,
                node.getRight()
            ));
        } else if (cmp > 0) {
            TreeNode<K, V> newRight = remove(node.getRight(), key);
            return balance(new TreeNode<>(
                node.getKey(), node.getValue(),
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
            TreeNode<K, V> minNode = findMin(node.getRight());
            return balance(new TreeNode<>(
                minNode.getKey(), minNode.getValue(),
                node.getLeft(),
                removeMin(node.getRight())
            ));
        }
    }

    private TreeNode<K, V> get(final TreeNode<K, V> node, final K key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            return get(node.getLeft(), key);
        } else if (cmp > 0) {
            return get(node.getRight(), key);
        } else {
            return node;
        }
    }

    private boolean containsValue(final TreeNode<K, V> node, final V value) {
        if (node == null) {
            return false;
        }

        if (Objects.equals(value, node.getValue())) {
            return true;
        }

        return containsValue(node.getLeft(), value)
            || containsValue(node.getRight(), value);
    }

    private TreeNode<K, V> findMin(final TreeNode<K, V> node) {
        TreeNode<K, V> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    private TreeNode<K, V> findMax(final TreeNode<K, V> node) {
        TreeNode<K, V> current = node;
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current;
    }

    private TreeNode<K, V> removeMin(final TreeNode<K, V> node) {
        if (node.getLeft() == null) {
            return node.getRight();
        }
        TreeNode<K, V> newLeft = removeMin(node.getLeft());
        return balance(new TreeNode<>(
            node.getKey(), node.getValue(),
            newLeft,
            node.getRight()
        ));
    }

    private TreeNode<K, V> balance(final TreeNode<K, V> node) {
        if (node == null) {
            return null;
        }

        int balanceFactor = getBalanceFactor(node);

        // Left heavy
        if (balanceFactor > 1) {
            if (getBalanceFactor(node.getLeft()) < 0) {
                // Left-Right case
                return rotateRight(new TreeNode<>(
                    node.getKey(), node.getValue(),
                    rotateLeft(node.getLeft()),
                    node.getRight()
                ));
            }
            // Left-Left case
            return rotateRight(node);
        }

        // Right heavy
        if (balanceFactor < -1) {
            if (getBalanceFactor(node.getRight()) > 0) {
                // Right-Left case
                return rotateLeft(new TreeNode<>(
                    node.getKey(), node.getValue(),
                    node.getLeft(),
                    rotateRight(node.getRight())
                ));
            }
            // Right-Right case
            return rotateLeft(node);
        }

        return node;
    }

    private int getBalanceFactor(final TreeNode<K, V> node) {
        if (node == null) {
            return 0;
        }
        return height(node.getLeft()) - height(node.getRight());
    }

    private int height(final TreeNode<K, V> node) {
        return node == null ? 0 : node.getHeight();
    }

    private TreeNode<K, V> rotateRight(final TreeNode<K, V> y) {
        TreeNode<K, V> x = y.getLeft();
        TreeNode<K, V> t2 = x.getRight();

        return new TreeNode<>(
            x.getKey(), x.getValue(),
            x.getLeft(),
            new TreeNode<>(y.getKey(), y.getValue(), t2, y.getRight())
        );
    }

    private TreeNode<K, V> rotateLeft(final TreeNode<K, V> x) {
        TreeNode<K, V> y = x.getRight();
        TreeNode<K, V> t2 = y.getLeft();

        return new TreeNode<>(
            y.getKey(), y.getValue(),
            new TreeNode<>(x.getKey(), x.getValue(), x.getLeft(), t2),
            y.getRight()
        );
    }

    private boolean isBalanced(final TreeNode<K, V> node) {
        if (node == null) {
            return true;
        }

        int balanceFactor = getBalanceFactor(node);
        if (Math.abs(balanceFactor) > 1) {
            return false;
        }

        return isBalanced(node.getLeft()) && isBalanced(node.getRight());
    }

    private List<Map.Entry<K, V>> inOrderTraversal() {
        List<Map.Entry<K, V>> result = new ArrayList<>();
        inOrderTraversal(root, result);
        return result;
    }

    private void inOrderTraversal(final TreeNode<K, V> node,
        final List<Map.Entry<K, V>> result) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), result);
            result.add(new AbstractMap.SimpleEntry<>(
                node.getKey(), node.getValue()));
            inOrderTraversal(node.getRight(), result);
        }
    }

    /**
     * Returns a snapshot of the current map state.
     * Useful for getting a consistent view during transactions.
     */
    public PersistentTreeMap<K, V> snapshot() {
        return this.deepCopy();
    }

    @Override
    public final String toString() {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<K, V> entry : this) {
            entries.add(entry.getKey() + "=" + entry.getValue());
        }
        return "PersistentTreeMap{" + String.join(", ", entries) + "}";
    }
}
