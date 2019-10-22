package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A container for a value that is loaded lazily.
 * @param <T> The type of data that is lazily loaded.
 */
public class Lazy<T> {
    private final Supplier<T> supplier;
    private volatile boolean valueCreated;
    private volatile T value;

    /**
     * Creates a new {@link Lazy}
     * @param supplier A {@link Supplier} that supplies the value when it is needed.
     */
    @Contract(pure = true, value = "null -> fail")
    public Lazy(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null.");
    }

    /**
     * Gets the value that is contained within this {@link Lazy}. If the value was not created, it will be before it
     * is returned.
     * @return The value.
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public T getValue() {
        if (valueCreated) {
            return value;
        }
        synchronized (this) {
            value = supplier.get();
            valueCreated = true;
        }

        return value;
    }

    /**
     * Returns a boolean value indicating whether the value in this {@link Lazy} was created.
     * @return A boolean value indicating whether the value in this {@link Lazy} was created.
     */
    @SuppressWarnings("unused")
    public boolean isValueCreated() {
        return valueCreated;
    }
}
