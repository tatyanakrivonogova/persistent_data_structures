package persistent.utils;

/**
 * A node for doubly-linked list implementations of persistent structures.
 * This class represents a single element in a linked list with references
 * to both previous and next nodes.
 * 
 * @param <T> the type of data stored in the node
 * @version 1.0
 */
public class Node<T> {
    /**
     * The data stored in this node.
     */
    private final T data;
    
    /**
     * Reference to the previous node in the list.
     */
    private final Node<T> prev;
    
    /**
     * Reference to the next node in the list.
     */
    private final Node<T> next;
    
    /**
     * Constructs a new node with the specified data.
     * 
     * @param data the data to store in this node
     */
    public Node(T data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }
    
    /**
     * Private constructor for creating nodes with all fields.
     * Used internally for copy operations.
     */
    private Node(T data, Node<T> prev, Node<T> next) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }
    
    /**
     * Returns the data stored in this node.
     * 
     * @return the data of this node
     */
    public T getData() {
        return data;
    }
    
    /**
     * Returns the previous node in the list.
     * 
     * @return the previous node, or null if this is the first node
     */
    public Node<T> getPrev() {
        return prev;
    }
    
    /**
     * Returns the next node in the list.
     * 
     * @return the next node, or null if this is the last node
     */
    public Node<T> getNext() {
        return next;
    }
    
    /**
     * Creates a new node with the same data but different previous node reference.
     * 
     * @param newPrev the new previous node
     * @return a new node with updated previous reference
     */
    public Node<T> withPrev(Node<T> newPrev) {
        return new Node<>(this.data, newPrev, this.next);
    }
    
    /**
     * Creates a new node with the same data but different next node reference.
     * 
     * @param newNext the new next node
     * @return a new node with updated next reference
     */
    public Node<T> withNext(Node<T> newNext) {
        return new Node<>(this.data, this.prev, newNext);
    }
    
    /**
     * Creates a new node with different data but same references.
     * 
     * @param newData the new data
     * @return a new node with updated data
     */
    public Node<T> withData(T newData) {
        return new Node<>(newData, this.prev, this.next);
    }
    
    /**
     * Creates a shallow copy of this node. The copied node will have
     * the same data and references to the same previous and next nodes.
     * 
     * @return a new node that is a copy of this node
     */
    public Node<T> copy() {
        return new Node<>(this.data, this.prev, this.next);
    }
}