package ahuber.hubble.utils;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * A tuple-like object containing the item in an indexed data structure like (e.g., an array or a
 * {@link java.util.List}) and its index.
 */
public @Data class IndexedValue<T> {

    /**
     * The index at which the item in the indexed data structure appears.
     */
    public final int index;

    /**
     * The item in the indexed data structure.
     */
    @Nullable public final T item;

    /**
     * Creates a new {@link IndexedValue}
     * @param index The index at which the item in the indexed data structure appears.
     * @param item The item in the indexed data structure.
     */
    public IndexedValue(int index, @Nullable T item) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format("'index' (%d) cannot be negative.", index));
        }

        this.index = index;
        this.item = item;
    }
}
