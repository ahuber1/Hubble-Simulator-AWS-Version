package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

/**
 * An {@link ArrayWrapper} for an {@code int} array.
 */
public class IntArrayWrapper implements ArrayWrapper<Integer> {
    @NotNull private final int[] array;

    /**
     * Creates a new {@link IntArrayWrapper} that wraps an underlying {@code int} array filled with zeroes and that
     * has the specified length.
     * @param length THe length of the {@code int} array
     */
    @Contract(pure = true)
    public IntArrayWrapper(int length) {
        this(new int[length]);
    }

    /**
     * Creates a new {@link IntArrayWrapper} that wraps the underlying {@code int} array.
     * @param array The {@code int} array.
     */
    @Contract(pure = true)
    public IntArrayWrapper(@NotNull int[] array) {
        this.array = array;
    }

    /**
     * Gets the underlying {@code int} array this {@link IntArrayWrapper} wraps.
     * @return The underlying {@code int} array.
     */
    public int[] getArray() {
        return array;
    }


    @Override
    public int length() {
        return array.length;
    }

    @Override
    public Integer get(int index) {
        return array[index];
    }

    @Override
    public void swap(int index1, int index2) {
        int temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }

    /**
     * Sets the item in the underlying array at the specified index.
     * @param item The item to add to the underlying array.
     * @param index The index at which to add {@code item} to the underlying array.
     */
    public void set(int item, int index) {
        array[index] = item;
    }

    @Override
    public void set(int index, @Nullable Integer item) {
        int value = item == null ? 0 : item;
        set (value, index);
    }

    @Override
    public void copyArray(int srcPos, @NotNull Object dest, int destPos, int length) {
        for (int i = 0; i < length; i++) {
            Integer value = array[i + srcPos];
            Array.set(dest, i + destPos, value);
        }
    }
}
