package persistent.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import persistent.core.PersistentStructure;
import persistent.core.SimpleVersion;
import persistent.core.Version;

/**
 * Partially persistent doubly linked list implemented using the fat-node
 * technique. Only the most recent version is mutable, older ones remain
 * immutable. Nodes store versioned references for prev/next so the
 * structure is shared efficiently.
 *
 * @param <E> element type
 */
@SuppressWarnings({"LineLength", "LongLine", "MaxLineLength", "HiddenField"})
public final class PersistentDoublyLinkedListFat<E extends Comparable<E>>
        implements PersistentStructure<E> {

    /**
     * Version-tagged reference stored inside a fat-node.
     *
     * @param <T> node element type
     */
    private static final class VersionedRef<T> {

        /**
         * Referred node (may be {@code null}).
         */
        private Node<T> value;

        /**
         * Version when this value was assigned.
         */
        private final int version;

        VersionedRef(final Node<T> value, final int version) {
            this.value = value;
            this.version = version;
        }

        Node<T> getValue() {
            return value;
        }

        int getVersion() {
            return version;
        }
    }

    /**
     * Doubly linked list node with fat-node references.
     *
     * @param <T> element type
     */
    private static final class Node<T> {

        /**
         * Immutable element value.
         */
        private final T value;

        /**
         * Versioned reference to the previous node.
         */
        private VersionedRef<T> prev;

        /**
         * Versioned reference to the next node.
         */
        private VersionedRef<T> next;

        Node(final T value, final Node<T> prev,
             final Node<T> next, final int version) {

            this.value = value;
            this.prev = new VersionedRef<>(prev, version);
            this.next = new VersionedRef<>(next, version);
        }

        T getValue() {
            return value;
        }

        VersionedRef<T> getPrevRef() {
            return prev;
        }

        VersionedRef<T> getNextRef() {
            return next;
        }

        void setPrevRef(final VersionedRef<T> ref) {
            this.prev = ref;
        }

        void setNextRef(final VersionedRef<T> ref) {
            this.next = ref;
        }
    }

    /**
     * Head of the list for this version.
     */
    private final Node<E> head;

    /**
     * Tail of the list for this version.
     */
    private final Node<E> tail;

    /**
     * Number of elements in the list.
     */
    private final int size;

    /**
     * Version identifier for this snapshot.
     */
    private final int versionId;

    /**
     * Creates an empty persistent list (version {@code 0}).
     */
    public PersistentDoublyLinkedListFat() {
        this(null, null, 0, 0);
    }

    /**
     * Full constructor used internally.
     *
     * @param head list head
     * @param tail list tail
     * @param size list size
     * @param versionId version
     */
    private PersistentDoublyLinkedListFat(final Node<E> head,
                                          final Node<E> tail,
                                          final int size,
                                          final int versionId) {
        this.head = head;
        this.tail = tail;
        this.size = size;
        this.versionId = versionId;
    }

    /**
     * Returns a read-only snapshot sharing all nodes.
     *
     * @return snapshot list
     */
    private PersistentDoublyLinkedListFat<E> snapshotVersion() {
        return new PersistentDoublyLinkedListFat<>(head, tail, size, versionId);
    }

    /**
     * Creates a new mutable version object.
     *
     * @return new structure wrapper with {@code versionId + 1}
     */
    private PersistentDoublyLinkedListFat<E> fork() {
        return new PersistentDoublyLinkedListFat<>(head, tail, size, versionId + 1);
    }

    @Override
    public PersistentStructure<E> createWithAdded(final E element) {
        return add(size, element);
    }

    @Override
    public PersistentStructure<E> createWithRemoved(final E element) {
        Node<E> current = head;
        int index = 0;

        while (current != null) {
            if (Objects.equals(current.getValue(), element)) {
                return remove(index);
            }
            current = getNext(current);
            index++;
        }
        return this;
    }

    @Override
    public PersistentStructure<E> createEmpty() {
        return new PersistentDoublyLinkedListFat<>();
    }

    @Override
    public boolean containsElement(final E element) {
        Node<E> current = head;

        while (current != null) {
            if (Objects.equals(current.getValue(), element)) {
                return true;
            }
            current = getNext(current);
        }
        return false;
    }

    @Override
    public Version getVersion() {
        return new SimpleVersion();
    }

    @Override
    public PersistentStructure<E> snapshot() {
        return snapshotVersion();
    }

    @Override
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object o) {
        try {
            return containsElement((E) o);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private Node<E> cursor = head;

            @Override
            public boolean hasNext() {
                return cursor != null;
            }

            @Override
            public E next() {
                if (cursor == null) {
                    throw new NoSuchElementException();
                }
                E value = cursor.getValue();
                cursor = getNext(cursor);
                return value;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;

        for (E element : this) {
            result[i++] = element;
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        T[] result = array;

        if (array.length < size) {
            result = (T[]) java.lang.reflect.Array.newInstance(
                    array.getClass().getComponentType(), size);
        }

        int i = 0;
        for (E element : this) {
            result[i++] = (T) element;
        }

        if (result.length > size) {
            result[size] = null;
        }
        return result;
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        for (Object item : collection) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the list element at a given index.
     *
     * @param index position of element
     * @return element value
     */
    public E get(final int index) {
        checkIndex(index);
        return nodeAt(index).getValue();
    }

    /**
     * Put the element in the beginning.
     *
     * @param value  element to put
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> addFirst(final E value) {
        return add(0, value);
    }

    /**
     * Put the element in the end.
     *
     * @param value element to put
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> addLast(final E value) {
        return add(size, value);
    }

    /**
     * Remove the element in the beginning.
     *
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> removeFirst() {
        return remove(0);
    }

    /**
     * Remove the element in the end.
     *
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> removeLast() {
        return remove(size - 1);
    }

    /**
     * Inserts a value at a given index, modifying only the newest version.
     *
     * @param value element to put
     * @param index where to put
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> add(
            final int index,
            final E value) {

        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }

        PersistentDoublyLinkedListFat<E> newVersion = fork();
        int version = newVersion.versionId;

        if (size == 0) {
            Node<E> single = new Node<>(value, null, null, version);
            return new PersistentDoublyLinkedListFat<>(single, single, 1, version);
        }

        if (index == size) {
            Node<E> newTail = new Node<>(value, newVersion.tail, null, version);
            setNext(newVersion.tail, newTail, version);
            return new PersistentDoublyLinkedListFat<>(
                    newVersion.head, newTail, size + 1, version);
        }

        if (index == 0) {
            Node<E> newHead = new Node<>(value, null, newVersion.head, version);
            setPrev(newVersion.head, newHead, version);
            return new PersistentDoublyLinkedListFat<>(
                    newHead, newVersion.tail, size + 1, version);
        }

        Node<E> current = newVersion.nodeAt(index);
        Node<E> prevNode = getPrev(current);
        Node<E> newNode = new Node<>(value, prevNode, current, version);

        setNext(prevNode, newNode, version);
        setPrev(current, newNode, version);

        return new PersistentDoublyLinkedListFat<>(
                newVersion.head, newVersion.tail, size + 1, version);
    }

    /**
     * Removes element at index using fat-node updates.
     *
     * @param index what to remove
     * @return new list version.
     */
    public PersistentDoublyLinkedListFat<E> remove(final int index) {
        checkIndex(index);

        PersistentDoublyLinkedListFat<E> newVersion = fork();
        int version = newVersion.versionId;

        Node<E> current = newVersion.nodeAt(index);
        Node<E> prevNode = getPrev(current);
        Node<E> nextNode = getNext(current);

        if (prevNode == null) {
            if (nextNode != null) {
                setPrev(nextNode, null, version);
            }
            return new PersistentDoublyLinkedListFat<>(
                    nextNode, newVersion.tail, size - 1, version);
        }

        if (nextNode == null) { // remove tail
            setNext(prevNode, null, version);
            return new PersistentDoublyLinkedListFat<>(
                    newVersion.head, prevNode, size - 1, version);
        }

        setNext(prevNode, nextNode, version);
        setPrev(nextNode, prevNode, version);

        return new PersistentDoublyLinkedListFat<>(
                newVersion.head, newVersion.tail, size - 1, version);
    }

    /**
     * Returns the node at a given index.
     *
     * @param index index in the list
     * @return node at index
     */
    private Node<E> nodeAt(final int index) {
        Node<E> cursor;

        if (index < (size >>> 1)) {
            cursor = head;
            for (int i = 0; i < index; i++) {
                cursor = getNext(cursor);
            }
        } else {
            cursor = tail;
            for (int i = size - 1; i > index; i--) {
                cursor = getPrev(cursor);
            }
        }

        return cursor;
    }

    /**
     * Ensures that index is within list bounds.
     *
     * @param index index to check
     */
    private void checkIndex(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns next pointer for given node in current version.
     *
     * @param node the target node
     * @return new nnode
     */
    private Node<E> getNext(final Node<E> node) {
        VersionedRef<E> ref = node.getNextRef();
        return ref.getVersion() <= versionId ? ref.getValue() : null;
    }

    /**
     * Returns previous pointer for given node in current version.
     *
     * @param node the target node
     * @return new node
     */
    private Node<E> getPrev(final Node<E> node) {
        VersionedRef<E> ref = node.getPrevRef();
        return ref.getVersion() <= versionId ? ref.getValue() : null;
    }

    /**
     * Writes next pointer to a node.
     *
     * @param node the target node
     * @param next what to write
     * @param version version on new node
     */
    private void setNext(final Node<E> node,
                         final Node<E> next,
                         final int version) {
        node.setNextRef(new VersionedRef<>(next, version));
    }

    /**
     * Writes prev pointer to a node.
     *
     * @param node the target node
     * @param prev what to write
     * @param version version on new node
     */
    private void setPrev(final Node<E> node,
                         final Node<E> prev,
                         final int version) {
        node.setPrevRef(new VersionedRef<>(prev, version));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Collection<?>)) {
            return false;
        }

        Collection<?> other = (Collection<?>) obj;
        if (size != other.size()) {
            return false;
        }

        Iterator<E> it1 = iterator();
        Iterator<?> it2 = other.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }

        return !it1.hasNext() && !it2.hasNext();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        for (E element : this) {
            result = prime * result + (element == null ? 0 : element.hashCode());
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        Node<E> cursor = head;

        while (cursor != null) {
            builder.append(cursor.getValue());
            cursor = getNext(cursor);

            if (cursor != null) {
                builder.append(", ");
            }
        }

        return builder.append(']').toString();
    }
}
