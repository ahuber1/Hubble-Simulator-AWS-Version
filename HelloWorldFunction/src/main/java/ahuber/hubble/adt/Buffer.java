package ahuber.hubble.adt;

import org.jetbrains.annotations.NotNull;

/**
 * A temporary region of memory in which data is stored while it is being processed or transferred.
 * @param <T> The type of data stored in this buffer.
 */
public class Buffer<T> extends BufferBase<T, Buffer<T>>
{
    /**
     * Creates a new buffer using the specified array underneath. The length of the array is also the
     * capacity of this buffer.
     * @param array The array.
     * @throws NullPointerException If {@code array} is null.
     * @throws IllegalArgumentException If the length of {@code array} is zero.
     */
    public Buffer(@NotNull T[] array) {
        this(new StandardArrayWrapper<>(array));
    }

    /**
     * Creates a new buffer using the specified {@link ArrayWrapper} underneath. The length of the
     * {@link ArrayWrapper} is also the capacity of this buffer
     * @param wrapper The {@link ArrayWrapper}
     * @throws NullPointerException If {@code wrapper} is {@code null}
     * @throws IllegalArgumentException If the length of {@code wrapper} is zero.
     */
    public Buffer(@NotNull ArrayWrapper<T> wrapper) {
        super(wrapper);
    }
}