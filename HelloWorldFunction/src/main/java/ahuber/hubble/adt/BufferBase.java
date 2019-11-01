package ahuber.hubble.adt;

import ahuber.hubble.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The base class for a temporary region of memory in which data is stored while it is being processed or transferred.
 * @param <T> The type of data the buffer stores.
 * @param <C> The type of collection {@link SizeObserver}s can receive.
 */
public class BufferBase<T, C extends BufferBase<T, C>> implements Collection<T> {
    private final List<SizeObserver<C>> observers = Collections.synchronizedList(new ArrayList<>());
    private final ArrayWrapper<T> wrapper;
    private int startIndex;
    private int endIndex;
    private int size;
    private boolean suppressObservers;

    /**
     * Creates a new {@link BufferBase} using the specified {@link ArrayWrapper} underneath. The length of the
     * {@link ArrayWrapper} is also the capacity of this {@link BufferBase}
     * @param wrapper The {@link ArrayWrapper}
     * @throws NullPointerException If {@code wrapper} is {@code null}
     * @throws IllegalArgumentException If the length of {@code wrapper} is zero.
     */
    protected BufferBase(@NotNull ArrayWrapper<T> wrapper) {
        this.wrapper = Objects.requireNonNull(wrapper, "Array cannot be null.");

        if (wrapper.length() < 1) {
            String message = String.format("The ArrayWrapper<T> cannot have a length of zero. " +
                    "Length was %d.", wrapper.length());
            throw new IllegalArgumentException(message);
        }
    }

    // region Register/Unregister Observers

    /**
     * Registers a {@link SizeObserver} with this buffer so it can be notified of changes in the
     * buffers size.
     * @param observer The {@link SizeObserver} to register.
     * @see #unregisterObserver(SizeObserver)
     */
    public synchronized void registerObserver(@Nullable SizeObserver<C> observer) {
        observers.add(observer);
    }

    /**
     * Unregisters a {@link SizeObserver} with this buffer so it is no longer notified of changes in the
     * buffer's size.
     * @param observer The {@link SizeObserver} to unregister.
     * @return {@code true} if the {@link SizeObserver} was found and unregistered.
     */
    public synchronized boolean unregisterObserver(@Nullable SizeObserver<C> observer) {
        return observers.remove(observer);
    }

    // endregion Register/Unregister Observers

    /**
     * Returns a boolean value indicating whether this buffer is full, i.e., if {@link #size()} equals
     * {@link #capacity()}
     * @return {@code true} If this {@link BufferBase} is full.
     */
    public synchronized boolean isFull() {
        return size() == capacity();
    }

    /**
     * Gets the buffer's capacity, i.e., the maximum number of items it can hold.
     * @return The buffer's capacity.
     * @see #size()
     */
    public synchronized int capacity() {
        return wrapper.length();
    }

    /**
     * Gets the number of elements in this buffer.
     * @return The number of elements in this buffer.
     * @see #capacity()
     */
    @Contract(pure = true)
    @Override
    public synchronized int size() {
        return size;
    }

    /**
     * Gets a boolean indicating whether the buffer is empty or not.
     * @return {@code true} if this buffer is empty (i.e., if {@link #size()} {@code == 0}) or {@code false} if it is
     * not.
     */
    @Contract(pure = true)
    @Override
    public synchronized boolean isEmpty() {
        return size == 0;
    }

    /**
     * Performs a linear search for the specified object using {@link Objects#equals(Object, Object)} and returns a
     * boolean indicating whether {@link Objects#equals(Object, Object)} returned {@code true} for at least one
     * element in this buffer.
     * @param o The object to search for.
     * @return {@code true} if {@code o} is contained within this buffer.
     */
    @Contract(pure = true)
    @Override
    public synchronized boolean contains(@Nullable Object o) {
        return stream().anyMatch(item -> Objects.equals(item, o));
    }

    /**
     * Gets an iterator that can iterate over this buffer from the first element of the buffer to the last.
     * @return The iterator.
     */
    @NotNull
    @Override
    public synchronized Iterator<T> iterator() {
        return new BufferIterator(new IndexedIterator(this));
    }

    // region toArray

    /**
     * Copies all the elements of this buffer from its first element to its last in order to an {@code Object[]}.
     * @return An {@code Object[]} containing all the copied elements.
     */
    @NotNull
    @Override
    public synchronized Object[] toArray() {
        return (Object[]) copyToArray(new Object[size()]);
    }

    /**
     * Copies all the elements of this buffer from the first element to its last in order to the specified array. If
     * the array's length is insufficient to accommodate all the elements of this collection, then it is lengthened
     * automatically so it accommodates {@link #size()} elements.
     * @param array The destination array.
     * @param <T1> The type of elements stored in {@code array}.
     * @return {@code array} with all of the buffer's contents copied into it.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public synchronized <T1> T1[] toArray(@NotNull T1[] array) {
        if (array.length < size()) {
            array = Arrays.copyOf(array, size());
        }

        return (T1[]) copyToArray(array);
    }

    // endregion toArray

    // region add/remove

    /**
     * Add the provided item to the end of the buffer if there is enough space.
     * @param item The item to add to the buffer
     * @return {@code false} if this buffer {@link #isFull()} is full} and {@code item} was not added,
     * {@code true} if this buffer is not full and the item was added.
     */
    @Override
    public synchronized boolean add(@Nullable T item) {
        if (isFull()) {
            return false;
        }

        if (!isEmpty()) {
            endIndex = incrementIndex(endIndex, wrapper.length());

            if (startIndex == endIndex) {
                startIndex = incrementIndex(startIndex, wrapper.length());
            }
        }

        wrapper.set(endIndex, item);
        incrementSize();
        return true;
    }

    /**
     * Removes the first occurrence of the provided item from the buffer, and shifts all the remaining items
     * in the buffer to the left by one. {@link Objects#equals(Object, Object)} is used to compare each item
     * in the {@link BufferBase} with the provided item.
     * @param item The item to remove.
     * @return {@code true} if {@code item} was found in the buffer and removed.
     */
    @Override
    public synchronized boolean remove(@Nullable Object item) {
        IndexedIterator iterator = new IndexedIterator(this);
        int previousPosition = -1;

        while (iterator.hasNext()) {
            IndexedItem indexedItem = iterator.next();

            // If we did not find the item to remove
            if (previousPosition == -1) {
                if (Objects.equals(indexedItem.getItem(), item)) {
                    previousPosition = indexedItem.getIndex();
                }

                continue;
            }

            // If we did find the item to remove, swap
            int currentPosition = indexedItem.getIndex();
            wrapper.swap(previousPosition, currentPosition);
            previousPosition = currentPosition;
        }

        // If we did NOT find the item to remove
        if (previousPosition == -1) {
            return false;
        }

        endIndex = decrementIndex(endIndex, wrapper.length());
        decrementSize();
        return true;
    }

    // endregion add/remove

    /**
     * Iterates through the buffer to determine if each element is also contained in the provided collection.
     * @param collection The other collection.
     * @return {@code true} if all elements in {@code collection} are also present in this buffer.
     */
    @Override
    public synchronized boolean containsAll(@NotNull Collection<?> collection) {
        if (collection.isEmpty() && this.isEmpty()) {
            return true;
        }

        // Because of the if-block above, the || operator in the condition below effectively becomes XOR
        if (collection.isEmpty() || this.isEmpty()) {
            return false;
        }

        return collection.stream().allMatch(this::contains);
    }

    /**
     * Adds all of the items in the specified collection to the buffer.
     * @param collection The collection.
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code collection}
     * is empty) or if the buffer does not have enough remaining space to accommodate all the items in the collection.
     * @throws NullPointerException If {@code collection} is {@code null}
     */
    @Override
    public synchronized boolean addAll(@NotNull Collection<? extends T> collection) {
        return addAll(collection, true);
    }

    /**
     * Adds all of the items in the specified collection to the buffer
     * @param collection The collection.
     * @param failIfInsufficientSpace A boolean value indicating whether this method should immediately fail if the
     *                                number of items in the collection is greater than the remaining space in the
     *                                buffer ({@code true}), or if this method should add elements from the start of
     *                                the collection to the end of the collection until there is no more space left
     *                                in the buffer ({@code false}).
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code collection}
     * is empty), or if {@code failIfInsufficientSpace} is {@code true} and the buffer does not have enough remaining
     * space to accommodate all the items in the collection.
     * @throws NullPointerException If {@code collection} is {@code null}.
     */
    public synchronized boolean addAll(@NotNull Collection<? extends T> collection, boolean failIfInsufficientSpace) {
        return addAll(Objects.requireNonNull(collection, "The collection cannot be null."), collection.size(),
                failIfInsufficientSpace);
    }


    /**
     * Adds all of the items in the specified array to the buffer.
     * @param array The array.
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code array}
     * is empty) or if the buffer does not have enough remaining space to accommodate all the items in the array.
     * @throws NullPointerException If {@code array} is {@code null}
     */
    @SuppressWarnings("unused")
    public synchronized boolean addAll(@NotNull T[] array) {
        return addAll(array, true);
    }

    // region addAll

    /**
     * Adds all of the items in the specified array to the buffer
     * @param array The array.
     * @param failIfInsufficientSpace A boolean value indicating whether this method should immediately fail if the
     *                                number of items in the array is greater than the remaining space in the
     *                                buffer ({@code true}), or if this method should add elements from the start of
     *                                the array to the end of the array until there is no more space left
     *                                in the buffer ({@code false}).
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code array}
     * is empty), or if {@code failIfInsufficientSpace} is {@code true} and the buffer does not have enough remaining
     * space to accommodate all the items in the array.
     * @throws NullPointerException If {@code array} is {@code null}.
     */
    public synchronized boolean addAll(@NotNull T[] array, boolean failIfInsufficientSpace) {
        ArrayWrapper<T> wrapper = new StandardArrayWrapper<>(Objects.requireNonNull(array, "The array cannot be null"));
        return addAll(wrapper, failIfInsufficientSpace);
    }

    /**
     * Adds all of the items in the specified {@link ArrayWrapper} to the buffer.
     * @param wrapper The {@link ArrayWrapper}.
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code wrapper}
     * is empty) or if the buffer does not have enough remaining space to accommodate all the items in the
     * {@link ArrayWrapper}.
     * @throws NullPointerException If {@code wrapper} is {@code null}
     */
    public synchronized boolean addAll(@NotNull ArrayWrapper<T> wrapper) {
        return addAll(wrapper, true);
    }

    /**
     * Adds all of the items in the specified {@link ArrayWrapper} to the buffer
     * @param wrapper The {@link ArrayWrapper}.
     * @param failIfInsufficientSpace A boolean value indicating whether this method should immediately fail if the
     *                                number of it ems in the {@link ArrayWrapper} is greater than the remaining
     *                                space in the buffer ({@code true}), or if this method should add elements from
     *                                the start of the {@link ArrayWrapper} to the end of the {@link ArrayWrapper} until
     *                                there is no more space left in the buffer ({@code false}).
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., {@code wrapper}
     * is empty), or if {@code failIfInsufficientSpace} is {@code true} and the buffer does not have enough remaining
     * space to accommodate all the items in the {@link ArrayWrapper}.
     * @throws NullPointerException If {@code wrapper} is {@code null}.
     */
    public synchronized boolean addAll(@NotNull ArrayWrapper<T> wrapper, boolean failIfInsufficientSpace) {
        return addAll(Objects.requireNonNull(wrapper, "The ArrayWrapper<T> cannot be null."), wrapper.length(),
                failIfInsufficientSpace);
    }

    private synchronized boolean addAll(@NotNull Iterable<? extends T> iterable, int size,
            boolean failIfInsufficientSpace) {

        return makeChangesEnMasse(() -> {
            int newSize = size() + size;

            if (failIfInsufficientSpace && newSize > capacity()) {
                return false;
            }

            boolean collectionChanged = false;

            for (T item : iterable) {
                boolean addSuccessful = add(item);
                collectionChanged = collectionChanged || addSuccessful;

                // Break from the loop if we can no longer add items.
                if (!addSuccessful) {
                    break;
                }
            }

            return collectionChanged;
        });
    }

    // endregion addAll

    // region removeAll

    /**
     * Removes all of the items in this buffer that are also contained in the provided collection.
     * @param collection A collection containing the elements to be removed from this collection.
     * @return {@code true} if the buffer changed as a result of the call.
     * @throws NullPointerException If {@code collection} is {@code null}.
     */
    @Override
    public synchronized boolean removeAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "The collection cannot be null.");
        return removeAll((Iterable<?>) collection);
    }

    /**
     * Removes all of the items in the buffer that are also contained in the provided {@link Iterable}.
     * @param iterable An {@link Iterable} containing the elements to be removed from the collection.
     * @return {@code true} if the buffer changed as a result of the call.
     * @throws NullPointerException If {@code iterable} is {@code null}.
     */
    public synchronized boolean removeAll(@NotNull Iterable<?> iterable) {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return makeChangesEnMasse(() -> {
            boolean collectionChanged = false;

            for (Object item : iterable) {
                boolean itemRemoved = remove(item);
                collectionChanged = collectionChanged || itemRemoved;
            }

            return collectionChanged;
        });
    }

    // endregion removeAll

    // region retainAll

    /**
     * Retrains only the elements in this buffer that are contained in the specified collection. In other words,
     * removes all items from the buffer that are not in the provided collection.
     * @param collection A collection containing elements to be retained in this buffer.
     * @return {@code true} if this buffer changed as a result of the call.
     * @throws NullPointerException If {@code collection} is {@code null}
     */
    @Override
    public synchronized boolean retainAll(@NotNull Collection<?> collection) {
        return retainAll((Iterable<?>) Objects.requireNonNull(collection, "The collection cannot be null."));
    }

    /**
     * Retains only the elements in this buffer that are contained in the provided {@link Iterable}. In other words,
     * removes all items from the buffer that are not in the provided {@link Iterable}.
     * @param iterable An {@link Iterable} containing elements to be retained in this buffer.
     * @return {@code true} if this buffer changed as a result of the call.
     * @throws NullPointerException If {@code iterable} is {@code null}
     */
    public synchronized boolean retainAll(@NotNull Iterable<?> iterable) {
        Objects.requireNonNull(iterable, "The iterable cannot be null");
        List<T> itemsToRemove = new ArrayList<>(size());

        for (T bufferItem : this) {
            boolean contains = false;

            if (iterable instanceof Collection<?>) {
                contains = ((Collection<?>)iterable).contains(bufferItem);
            } else {
                for (Object keepItem : iterable) {
                    if (!Objects.equals(keepItem, bufferItem)) {
                        continue;
                    }
                    contains = true;
                    break;
                }
            }

            if (contains) {
                continue;
            }

            itemsToRemove.add(bufferItem);
        }

        return removeAll(itemsToRemove);
    }

    // endregion retainAll

    // region take

    /**
     * Removes the first {@code n} elements from the buffer and returns them in an array.
     * @param n The number of elements to take from the buffer. This value is clamped in the range [0, {@link #size()}]
     * @param array An array where the elements taken from the buffer are copied into. If the length of the array is
     *              less than the value of {@code n} after it has been clamped in the range [0, {@link #size()}] (let
     *              this be {@code clampedN}), then the array's contents are copied into an array of length
     *              {@code clampedN}.
     * @return The first {@code n} elements from the buffer.
     */
    public synchronized T[] take(int n, @NotNull T[] array) {
        Objects.requireNonNull(array, "The array cannot be null.");
        return take(n, clampedN -> {
            T[] arrayCopy = toArray(array);
            arrayCopy = Arrays.copyOf(arrayCopy, clampedN);
            return new StandardArrayWrapper<>(arrayCopy);
        }).getArray();
    }

    /**
     * The worker function for the {@link #take(int, Object[])} method or any derivative of that method implemented
     * in derived classes.
     * @param n The number of elements to take from the buffer. This value is clamped in the range [0, {@link #size()}]
     * @param arrayCopySupplier A aws function that takes the value of {@code n} after it has been clamped into
     *                          the range [0, {@link #size()}] (let this be {@code clampedN}) and returns an
     *                          {@link ArrayWrapper} of length {@code clampedN} that contains the first
     *                          {@code clampedN} elements from the buffer.
     * @param <A> The {@link ArrayWrapper} type that {@code arrayCopySupplier} returns.
     * @return The {@link ArrayWrapper} returned by {@code arrayCopySupplier}
     */
    protected synchronized <A extends ArrayWrapper<T>> A take(int n, @NotNull Function<Integer, A> arrayCopySupplier) {
        return makeChangesEnMasse(() -> {
            int clampedN = Utils.clamp(n, 0, size() - 1);
            A arrayCopy = arrayCopySupplier.apply(clampedN);
            startIndex = incrementIndex(startIndex, wrapper.length(), arrayCopy.length());
            setSize(size() - arrayCopy.length());
            return arrayCopy;
        });
    }

    // endregion take

    /**
     * Clears the buffer of all elements current contained within.
     */
    @Override
    public synchronized void clear() {
        startIndex = 0;
        endIndex = 0;
        setSize(0);
    }

    /**
     * A method that executes the provided action while ensuring that any {@link SizeObserver}s are not notified of
     * any changes in this buffer's size until the action completes.
     * @param action A {@link Supplier} that performs the action and returns a value (typically a {@code boolean})
     *               indicating success/failure.
     * @param <T1> The type of data the {@link Supplier} returns.
     * @return The value returned by {@code action}.
     */
    protected synchronized <T1> T1 makeChangesEnMasse(@NotNull Supplier<T1> action) {
        int previousSize = size();

        try {
            suppressObservers = true;
            return action.get();
        } finally {
            suppressObservers = false;
            int currentSize = size();

            if (currentSize != previousSize) {
                invokeObservers();
            }
        }
    }

    // region pure methods that calculate indices

    @Contract(pure = true)
    private static int incrementIndex(int index, int length) {
        return incrementIndex(index, length, 1);
    }

    private static int incrementIndex(int index, int length, int amount) {
        amount = Utils.clamp(amount, 0, length);
        return (index + amount) % length;
    }

    @Contract(pure = true)
    private static int decrementIndex(int index, int length) {
        return (index + length - 1) % length;
    }

    // endregion pure methods that calculate indices

    // region methods that set the size of the buffer

    private synchronized void incrementSize() {
        setSize(size() + 1);
    }

    private synchronized void decrementSize() {
        setSize(size() - 1);
    }

    private synchronized void setSize(int newValue) {
        if (size == newValue) {
            return;
        }

        if (newValue < 0) {
            throw new IllegalArgumentException(String.format("size cannot be negative. Passed-in value was %d", newValue));
        }

        if (newValue > capacity()) {
            throw new IllegalArgumentException(String.format("Size cannot be greater than the capacity. The passed-in" +
                    " value was %d. The capacity is %d.", newValue, capacity()));
        }

        size = newValue;
        invokeObservers();
    }

    // endregion methods that set the size of the buffer

    @SuppressWarnings("unchecked")
    private synchronized void invokeObservers() {
        if (suppressObservers) {
            return;
        }

        for (SizeObserver<C> observer : observers) {
            if (observer != null) {
                observer.sizeChanged((C) this);
            }
        }
    }

    @Contract("_ -> param1")
    private synchronized Object copyToArray(@NotNull Object array) {
        if (isEmpty()) {
            return array;
        }

        if (startIndex <= endIndex) {
            int length = ArrayUtils.calculateLength(startIndex, endIndex, this.wrapper.length());
            this.wrapper.copyArray(startIndex, array, 0, length);
            return array;
        }

        int start = startIndex;
        int end = this.wrapper.length() - 1;
        int destination = 0;
        int length = ArrayUtils.calculateLength(start, end, this.wrapper.length());
        this.wrapper.copyArray(start, array, destination, length);

        start = 0;
        end = endIndex;
        destination += length;
        length = ArrayUtils.calculateLength(start, end, this.wrapper.length());
        this.wrapper.copyArray(start, array, destination, length);
        return array;
    }

    // region Inner classes

    private class IndexedItem {
        @Nullable private final T item;
        private final int index;

        @Contract(pure = true)
        IndexedItem(@Nullable T item, int index) {
            this.item = item;
            this.index = index;
        }

        int getIndex() {
            return index;
        }

        @Contract(pure = true)
        private @Nullable T getItem() {
            return item;
        }
    }

    private class IndexedIterator implements Iterator<IndexedItem> {
        @NotNull private final Object[] array;
        private int index;
        private int size;
        private int returnCount;

        @Contract(pure = true)
        private IndexedIterator(@NotNull BufferBase<T, C> bufferBase) {
            array = bufferBase.wrapper.copyArray();
            index = startIndex;
            size = bufferBase.size();
        }

        @Override
        public boolean hasNext() {
            return returnCount < size;
        }

        @SuppressWarnings("unchecked")
        @Override
        public IndexedItem next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            int index = this.index;
            this.index = incrementIndex(this.index, array.length);
            ++returnCount;
            return new IndexedItem((T) array[index], index);
        }
    }

    private class BufferIterator implements Iterator<T> {
        @NotNull private final IndexedIterator iterator;

        @Contract(pure = true)
        BufferIterator(@NotNull IndexedIterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next().getItem();
        }
    }

    // endregion Inner classes
}