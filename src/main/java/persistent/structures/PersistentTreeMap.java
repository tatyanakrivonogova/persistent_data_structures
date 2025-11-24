package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.utils.TreeNode;

import java.util.*;

/**
 * A persistent associative array based on AVL tree.
 * Provides efficient key-value storage with logarithmic time operations.
 * 
 * @param <K> the type of keys maintained by this map, must be Comparable
 * @param <V> the type of mapped values
 * @version 1.0
 */
public class PersistentTreeMap<K extends Comparable<K>, V> extends AbstractPersistentStructure<Map.Entry<K, V>> {
    private final TreeNode<K, V> root;
    private final int size;
    
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
    private PersistentTreeMap(TreeNode<K, V> root, int size, Version version) {
        super(version);
        this.root = root;
        this.size = size;
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * 
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return a new persistent map containing the specified key-value mapping
     */
    public PersistentTreeMap<K, V> put(K key, V value) {
        TreeNode<K, V> newRoot = put(root, key, value);
        boolean keyExists = get(root, key) != null;
        
        Version newVersion = createNewVersion();
        return new PersistentTreeMap<>(
            newRoot, 
            keyExists ? size : size + 1, 
            newVersion
        );
    }
    
    /**
     * Removes the mapping for the specified key from this map if present.
     * 
     * @param key the key whose mapping is to be removed from the map
     * @return a new persistent map without the specified key mapping
     */
    public PersistentTreeMap<K, V> remove(K key) {
        if (!containsKey(key)) {
            return this; // Key not found - return same instance
        }
        
        TreeNode<K, V> newRoot = remove(root, key);
        Version newVersion = createNewVersion();
        return new PersistentTreeMap<>(newRoot, size - 1, newVersion);
    }
    
    /**
     * Returns the value to which the specified key is mapped, or null if no mapping exists.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if not present
     */
    public V get(K key) {
        TreeNode<K, V> node = get(root, key);
        return node == null ? null : node.getValue();
    }
    
    /**
     * Returns true if this map contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    /**
     * Returns true if this map contains the specified value.
     * 
     * @param value the value whose presence in this map is to be tested
     * @return true if this map contains the specified value
     */
    public boolean containsValue(V value) {
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
    
    private TreeNode<K, V> put(TreeNode<K, V> node, K key, V value) {
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
    
    private TreeNode<K, V> remove(TreeNode<K, V> node, K key) {
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
    
    private TreeNode<K, V> get(TreeNode<K, V> node, K key) {
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
    
    private boolean containsValue(TreeNode<K, V> node, V value) {
        if (node == null) {
            return false;
        }
        
        if (Objects.equals(value, node.getValue())) {
            return true;
        }
        
        return containsValue(node.getLeft(), value) || containsValue(node.getRight(), value);
    }
    
    private TreeNode<K, V> findMin(TreeNode<K, V> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }
    
    private TreeNode<K, V> findMax(TreeNode<K, V> node) {
        while (node.getRight() != null) {
            node = node.getRight();
        }
        return node;
    }
    
    private TreeNode<K, V> removeMin(TreeNode<K, V> node) {
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
    
    private TreeNode<K, V> balance(TreeNode<K, V> node) {
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
    
    private int getBalanceFactor(TreeNode<K, V> node) {
        if (node == null) return 0;
        return height(node.getLeft()) - height(node.getRight());
    }
    
    private int height(TreeNode<K, V> node) {
        return node == null ? 0 : node.getHeight();
    }
    
    private TreeNode<K, V> rotateRight(TreeNode<K, V> y) {
        TreeNode<K, V> x = y.getLeft();
        TreeNode<K, V> T2 = x.getRight();
        
        return new TreeNode<>(
            x.getKey(), x.getValue(),
            x.getLeft(),
            new TreeNode<>(y.getKey(), y.getValue(), T2, y.getRight())
        );
    }
    
    private TreeNode<K, V> rotateLeft(TreeNode<K, V> x) {
        TreeNode<K, V> y = x.getRight();
        TreeNode<K, V> T2 = y.getLeft();
        
        return new TreeNode<>(
            y.getKey(), y.getValue(),
            new TreeNode<>(x.getKey(), x.getValue(), x.getLeft(), T2),
            y.getRight()
        );
    }
    
    private boolean isBalanced(TreeNode<K, V> node) {
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
    
    private void inOrderTraversal(TreeNode<K, V> node, List<Map.Entry<K, V>> result) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), result);
            result.add(new AbstractMap.SimpleEntry<>(node.getKey(), node.getValue()));
            inOrderTraversal(node.getRight(), result);
        }
    }
    
    @Override
    public String toString() {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<K, V> entry : this) {
            entries.add(entry.getKey() + "=" + entry.getValue());
        }
        return "PersistentTreeMap{" + String.join(", ", entries) + "}";
    }
}