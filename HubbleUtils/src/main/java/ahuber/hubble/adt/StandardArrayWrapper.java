package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Objects;

/**
 * An array wrapper that wraps an array of an object type (as opposed to a primitive type).
 * @param <T> The type of objects that are wrapped.
 */
public class StandardArrayWrapper<T> implements ArrayWrapper<T> {
    private final T[] array;

    /**
     * Creates a new {@link StandardArrayWrapper} that wraps the specified array.
     * @param array The array that this {@link StandardArrayWrapper} will wrap.
     * @throws NullPointerException If {@code array} is {@code null}
     */
    @Contract("null -> fail")
    public StandardArrayWrapper(@NotNull T[] array) {
        this(Objects.requireNonNull(array, "The array cannot be null."), array.length);
    }

    /**
     * Creates a new {@link StandardArrayWrapper} that wraps the specified array.
     * @param array The array that this {@link StandardArrayWrapper} will wrap.
     * @param length The length of {@code array}. If {@code length} is greater than the length of {@code array},
     * {@code array} is resized using {@link Arrays#copyOf(Object[], int)}
     * @throws IllegalArgumentException If {@code length} &lt; 0
     * @throws NullPointerException If {@code array} is {@code null}.
     */
    @Contract("null, _ -> fail")
    public StandardArrayWrapper(@NotNull T[] array, @Range(from = 0, to = Integer.MAX_VALUE) int length) {
        if (length < 0) {
            throw new IllegalArgumentException(String.format("The length cannot be negative. " +
                    "The length provided was %d", length));
        }

        if (Objects.requireNonNull(array, "The array cannot be null").length < length) {
            array = Arrays.copyOf(array, length);
        }

        this.array = array;
    }

    /**
     * Gets the array this {@link StandardArrayWrapper} wraps.
     * @return The array this {@link StandardArrayWrapper} wraps.
     */
    @NotNull
    public T[] getArray() {
        return array;
    }

    @Override
    public int length() {
        return array.length;
    }

    @Nullable
    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public void set(int index, @Nullable T item) {
        array[index] = item;
    }


    @SuppressWarnings("SuspiciousSystemArraycopy")
    @Override
    public void copyArray(int srcPos, @NotNull Object dest, int destPos, int length) {
        System.arraycopy(array, srcPos, dest, destPos, length);
    }
}
