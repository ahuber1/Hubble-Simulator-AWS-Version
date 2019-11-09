package ahuber.hubble.adt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

/**
 * An interface defining an object that wraps an array while allowing access to its internals and allowing it to be
 * used alongside generic type arguments.
 *
 * @param <T> The type of data stored in the underlying array.
 */
public interface ArrayWrapper<T> extends Iterable<T> {

    /**
     * Returns the length of the underlying array.
     *
     * @return The length of the underlying array.
     */
    int length();

    /**
     * Gets the item from the underlying array at the specified index.
     *
     * @param index The index.
     * @return The item in the underlying array at {@code index}
     */
    T get(int index);

    /**
     * Sets the item in the underlying array at the specified index.
     *
     * @param index The index at which to add {@code item} to the underlying array.
     * @param item  The item to add to the underlying array.
     */
    void set(int index, @Nullable T item);

    /**
     * Copies the elements from the underlying array to another array using
     * {@link System#arraycopy(Object, int, Object, int, int)}
     *
     * @param srcPos  The starting position in the underlying array.
     * @param dest    The destination array.
     * @param destPos The starting position in the destination data.
     * @param length  The number of array elements to be copied.
     */
    void copyArray(int srcPos, @NotNull Object dest, int destPos, int length);

    /**
     * Copies the underlying array to an array of type {@link Object}
     *
     * @return The array.
     */
    default Object[] copyArray() {
        return copyArray(new Object[length()]);
    }

    /**
     * Swaps the elements of the underlying array at the specified indices.
     *
     * @param index1 The first index.
     * @param index2 The second index.
     */
    default void swap(int index1, int index2) {
        T temp = get(index1);
        set(index1, get(index2));
        set(index2, temp);
    }

    /**
     * Copies the underlying array to an array of type {@code T1}
     *
     * @param dest The array where the underlying array is copied to.
     * @param <T1> The type of data in {@code dest}
     * @return The array with its contents copied from the underlying array.
     */
    default <T1> T1[] copyArray(@NotNull T1[] dest) {
        if (dest.length < length()) {
            dest = Arrays.copyOf(dest, length());
        }

        copyArray(0, dest, 0, length());
        return dest;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    @NotNull
    default Iterator<T> iterator() {
        return new ArrayWrapperIterator<>(this);
    }
}
