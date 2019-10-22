package ahuber.hubble.adt;

import ahuber.hubble.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * A temporary region of memory in which {@code int} data is stored while it is being processed or transferred
 */
public class IntBuffer extends BufferBase<Integer, IntBuffer> {

    /**
     * Creates a new buffer with the specified capacity.
     * @param capacity The capacity of the buffer. This value is clamped in the range [0, {@link Integer#MAX_VALUE}]
     */
    public IntBuffer(int capacity) {
        this(new int[Utils.clamp(capacity, 0, Integer.MAX_VALUE)]);
    }

    /**
     *  Creates a new buffer using the specified array underneath. The length of the array is also the capacity of
     *  this buffer.
     * @param array The array
     * @throws NullPointerException If {@code array} is {@code null}
     * @throws IllegalArgumentException If the length of {@code array} is zero.
     */
    public IntBuffer(@NotNull int[] array) {
        this(new IntArrayWrapper(Objects.requireNonNull(array, "The array cannot be null.")));
    }

    /**
     * Creates a new buffer using the specified {@link ArrayWrapper} underneath. The length of the
     * {@link ArrayWrapper} is also the capacity of this buffer.
     * @param wrapper The {@link ArrayWrapper}
     */
    public IntBuffer(@NotNull IntArrayWrapper wrapper) {
        super(wrapper);
    }

    /**
     * Adds all of the items listed to the buffer.
     * @param items The items to add.
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., if no items are
     * listed) or if the buffer does not have enough space to accommodate all the items.
     * @throws NullPointerException If {@code items} is {@code null}.
     */
    @SuppressWarnings("UnusedReturnValue")
    public synchronized boolean addAll(@NotNull int...items) {
        return addAll(true, items);
    }

    /**
     * Adds all of the items listed to the buffer.
     * @param failIfInsufficientSpace A boolean value indicating whether this method should immediately fail if the
     *                                number of items specified is greater than the remaining space of the buffer
     *                                ({@code true}), or if this method should add elements in the order they are
     *                                specified here until there is no more space in the buffer ({@code true}).
     * @param items The items to add to the buffer.
     * @return {@code true} if the buffer changed, {@code false} if the buffer did not change (e.g., there are no
     * items provided in {@code items}), or if {@code failIfInsufficientSpace} is {@code true} and the buffer does
     * not have enough remaining space to accommodate all of the items.
     * @throws NullPointerException If {@code items} is {@code null}.
     */
    public synchronized boolean addAll(boolean failIfInsufficientSpace, @NotNull int... items) {
        IntArrayWrapper wrapper = new IntArrayWrapper(Objects.requireNonNull(items, "The items cannot be null"));
        return addAll(wrapper, failIfInsufficientSpace);
    }

    /**
     * Removes all of the items in this buffer that are also provided in the variable parameter list/{@code int}
     * array.
     * @param items The {@code int} values to remove.
     * @return {@code true} if the buffer changed as a result of this call.
     * @throws NullPointerException If {@code items} is {@code null}.
     */
    @SuppressWarnings("unused")
    public synchronized boolean removeAll(@NotNull int...items) {
        return removeAll(new IntArrayWrapper(Objects.requireNonNull(items, "The items cannot be null")));
    }

    /**
     * Retains only the elements in the buffer that are contained in the provided variable parameter list/{@code int}
     * array.
     * @param items The items to be retained in the buffer.
     * @return {@code true} if the buffer changed as a result of this call.
     * @throws NullPointerException If {@code items} is {@code null}
     */
    @SuppressWarnings("unused")
    public synchronized boolean retainAll(@NotNull int...items) {
        return retainAll(new IntArrayWrapper(Objects.requireNonNull(items, "The items cannot be null")));
    }

    /**
     * Copies all the elements of this buffer from its first element to its last in order to an {@code int} array.
     * @return An {@code int} array containing all the copied elements.
     */
    @NotNull
    public synchronized int[] toIntArray() {
        return toIntArray(size());
    }

    /**
     * Copies the first {@code n} elements to an {@code int} array.
     * @param n The number of elements to copy. This value is clamped in the range [0, {@link #size()}]
     * @return An {@code int} array containing the first {@code n} elements.
     */
    public synchronized int[] toIntArray(int n) {
        n = Utils.clamp(n, 0, size());
        int[] array = new int[n];

        for (Integer integer : this) {
            if (n == 0) {
                break;
            }

            int value = integer == null ? 0 : integer;
            array[array.length - n] = value;
            --n;
        }

        return array;
    }

    /**
     * Removes the first {@code n} elements from the buffer and returns them in an {@code int} array.
     * @param n The number of elements to take from the buffer. This value is clamped in the range [0, {@link #size()}]
     * @return An array where the elements taken from the buffer are copied into. If the
     */
    public synchronized int[] take(int n) {
        return take(n, clampedN -> {
            int[] arrayCopy = toIntArray(clampedN);
            return new IntArrayWrapper(arrayCopy);
        }).getArray();
    }
}
