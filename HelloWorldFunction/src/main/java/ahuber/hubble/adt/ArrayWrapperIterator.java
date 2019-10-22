package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} for an {@link ArrayWrapper}
 * @param <T> The type of data stored in the {@link ArrayWrapper}
 */
public class ArrayWrapperIterator<T> implements Iterator<T> {
    @NotNull private final ArrayWrapper<T> wrapper;
    private int index;

    /**
     * Creates a new {@link ArrayWrapperIterator} that iterates over the provided {@link ArrayWrapper}
     * @param wrapper The {@link ArrayWrapper} to iterate through.
     */
    @Contract(pure = true)
    public ArrayWrapperIterator(@NotNull ArrayWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return index < wrapper.length();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        T item = wrapper.get(index);
        ++index;
        return item;
    }
}
