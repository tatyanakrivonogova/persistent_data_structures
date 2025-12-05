package persistent.structures;

import persistent.core.AbstractPersistentStructure;
import persistent.core.Version;
import persistent.core.TransactionalVersion;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Persistent, immutable doubly linked list implementation
 * with transaction support.
 *
 * @param <E> element type
 * @version 3.0
 */
public final class PersistentDoublyLinkedList<E extends Comparable<E>>
        extends AbstractPersistentStructure<E> {

    /**
     * Doubly linked list node.
     *
     * @param <T> element type
     */
    private static final class Node<T> {

        /** Node value (immutable). */
        private final T value;

        /** Previous node. */
        private Node<T> prev;

        /** Next node. */
        private Node<T> next;

        Node(final T nodeValue,
             final Node<T> nodePrev,
             final Node<T> nodeNext) {
            this.value = nodeValue;
            this.prev = nodePrev;
            this.next = nodeNext;
        }

        public T getValue() {
            return value;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public void setPrev(final Node<T> newPrev) {
            this.prev = newPrev;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(final Node<T> newNext) {
            this.next = newNext;
        }

        /**
         * Creates a deep copy of this node.
         *
         * @return a new Node instance with the same value and references
         * to the same previous and next nodes as this node
         */
        public Node<T> deepCopy() {
            return new Node<>(value, prev, next);
        }
    }

    /** First element. */
    private Node<E> head;

    /** Last element. */
    private Node<E> tail;

    /** Collection size. */
    private int size;

    /**
     * Full constructor used internally.
     *
     * @param newHead list head
     * @param newTail list tail
     * @param newSize list size
     * @param version structure version
     */
    private PersistentDoublyLinkedList(
            final Node<E> newHead,
            final Node<E> newTail,
            final int newSize,
            final Version version
    ) {
        super(version);
        this.head = newHead;
        this.tail = newTail;
        this.size = newSize;
    }

    /**
     * Creates an empty persistent doubly linked list.
     */
    public PersistentDoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Creates a deep copy of the current list.
     * 
     * @return a new {@code PersistentDoublyLinkedList} instance that is a deep copy
     * of the current list, containing copies of all nodes with their
     * structure preserved
     */
    private PersistentDoublyLinkedList<E> deepCopy() {
        if (head == null) {
            return new PersistentDoublyLinkedList<>(null, null, 0, getVersion());
        }

        // Deep copy all nodes
        Node<E> newHead = null;
        Node<E> newTail = null;
        Node<E> current = head;
        Node<E> prevCopy = null;

        while (current != null) {
            Node<E> nodeCopy = current.deepCopy();

            if (newHead == null) {
                newHead = nodeCopy;
            }

            if (prevCopy != null) {
                prevCopy.setNext(nodeCopy);
                nodeCopy.setPrev(prevCopy);
            }

            prevCopy = nodeCopy;
            current = current.getNext();
        }

        newTail = prevCopy;

        return new PersistentDoublyLinkedList<>(newHead, newTail, size, getVersion());
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
        if (preTransactionState instanceof PersistentDoublyLinkedList) {
            @SuppressWarnings("unchecked")
            PersistentDoublyLinkedList<E> savedState = 
                (PersistentDoublyLinkedList<E>) preTransactionState;
            this.head = savedState.head;
            this.tail = savedState.tail;
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
                setVersion(new TransactionalVersion(tv.getId(), null, false, 0));
            }
        }
    }

    /**
     * Checks if the list is empty.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns list size.
     *
     * @return list size
     */
    public int size() {
        return size;
    }

    /**
     * Returns element by index.
     *
     * @param index index of element
     * @return element value
     */
    public E get(final int index) {
        checkIndex(index);
        return nodeAt(index).getValue();
    }

    /**
     * Adds value to the beginning of the list.
     *
     * @param value new value
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> addFirst(final E value) {
        return add(0, value);
    }

    /**
     * Adds value to the end of the list.
     *
     * @param value new value
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> addLast(final E value) {
        return add(size, value);
    }

    /**
     * Inserts a value at a given index.
     *
     * @param index target index
     * @param value element to add
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> add(final int index, final E value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index: " + index);
        }

        final Version version = createNewVersion();

        if (size == 0) {
            final Node<E> newNode = new Node<>(value, null, null);
            PersistentDoublyLinkedList<E> result = new PersistentDoublyLinkedList<>(
                    newNode, newNode, 1, version
            );

            // If in transaction, update current instance
            if (isInTransaction()) {
                this.head = result.head;
                this.tail = result.tail;
                this.size = result.size;
                this.setVersion(version);
                return this;
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        Node<E>[] newHeadRef = new Node[]{null};
        Node<E> prevNewNode = null;
        Node<E> insertedNode = null;

        Node<E> curNode = head;
        int pos = 0;

        while (curNode != null) {
            if (pos == index) {
                insertedNode = new Node<>(value, prevNewNode, null);
                if (prevNewNode == null) {
                    newHeadRef[0] = insertedNode;
                } else {
                    prevNewNode.setNext(insertedNode);
                }
                insertedNode.setPrev(prevNewNode);
                prevNewNode = insertedNode;
            }

            prevNewNode = copyNode(curNode, newHeadRef, prevNewNode);

            curNode = curNode.getNext();
            pos++;
        }

        if (index == size) {
            final Node<E> lastNode = new Node<>(value, prevNewNode, null);
            if (prevNewNode == null) {
                newHeadRef[0] = lastNode;
            } else {
                prevNewNode.setNext(lastNode);
            }
            lastNode.setPrev(prevNewNode);
            prevNewNode = lastNode;
        }

        PersistentDoublyLinkedList<E> result = new PersistentDoublyLinkedList<>(
                newHeadRef[0], prevNewNode, size + 1, version
        );

        // If in transaction, update current instance
        if (isInTransaction()) {
            this.head = result.head;
            this.tail = result.tail;
            this.size = result.size;
            this.setVersion(version);
            return this;
        }

        return result;
    }

    /**
     * Removes the first element.
     *
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("removeFirst from empty list");
        }
        return remove(0);
    }

    /**
     * Removes the last element.
     *
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("removeLast from empty list");
        }
        return remove(size - 1);
    }

    /**
     * Removes element at index.
     *
     * @param index index of removable element
     * @return new list version
     */
    public PersistentDoublyLinkedList<E> remove(final int index) {
        checkIndex(index);

        final Version version = createNewVersion();

        if (size == 1) {
            PersistentDoublyLinkedList<E> result = new PersistentDoublyLinkedList<>(
                    null, null, 0, version
            );

            // If in transaction, update current instance
            if (isInTransaction()) {
                this.head = result.head;
                this.tail = result.tail;
                this.size = result.size;
                this.setVersion(version);
                return this;
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        Node<E>[] newHeadRef = new Node[]{null};
        Node<E> prevNewNode = null;

        Node<E> curNode = head;
        int pos = 0;

        while (curNode != null) {
            if (pos == index) {
                curNode = curNode.getNext();
                pos++;
                continue;
            }

            prevNewNode = copyNode(curNode, newHeadRef, prevNewNode);

            curNode = curNode.getNext();
            pos++;
        }

        PersistentDoublyLinkedList<E> result = new PersistentDoublyLinkedList<>(
                newHeadRef[0], prevNewNode, size - 1, version
        );

        // If in transaction, update current instance
        if (isInTransaction()) {
            this.head = result.head;
            this.tail = result.tail;
            this.size = result.size;
            this.setVersion(version);
            return this;
        }

        return result;
    }

    /**
     * Returns a snapshot of the current list state.
     * Useful for getting a consistent view during transactions.
     */
    public PersistentDoublyLinkedList<E> snapshot() {
        return this.deepCopy();
    }

    /**
     * Creates a copy of a node, links it to the
     * previous one and updates headRef.
     *
     * @param source original node
     * @param headRef reference to newHead
     * @param prevNew previous new node
     * @return newly created copied node
     */
    private Node<E> copyNode(
            final Node<E> source,
            final Node<E>[] headRef,
            final Node<E> prevNew
    ) {
        final Node<E> copied = new Node<>(
                source.getValue(), prevNew, null
        );
        if (prevNew == null) {
            headRef[0] = copied;
        } else {
            prevNew.setNext(copied);
        }
        copied.setPrev(prevNew);
        return copied;
    }

    /**
     * Get node at index.
     *
     * @param index node index
     *
     * @return node at index
     * */
    private Node<E> nodeAt(final int index) {
        Node<E> current;

        if (index < (size >> 1)) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.getPrev();
            }
        }

        return current;
    }

    /**
     * Validates index.
     *
     * @param index index to validate
     * */
    private void checkIndex(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "index: " + index + ", size: " + size
            );
        }
    }

    /**
     * Returns iterator over list elements.
     *
     * @return iterator
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private Node<E> cur = head;

            @Override
            public boolean hasNext() {
                return cur != null;
            }

            @Override
            public E next() {
                if (cur == null) {
                    throw new NoSuchElementException();
                }
                final E val = cur.getValue();
                cur = cur.getNext();
                return val;
            }
        };
    }

    /**
     * Returns string representation.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('[');

        Node<E> curNode = head;
        while (curNode != null) {
            sb.append(curNode.getValue());
            curNode = curNode.getNext();
            if (curNode != null) {
                sb.append(", ");
            }
        }

        sb.append(']');
        return sb.toString();
    }
}
