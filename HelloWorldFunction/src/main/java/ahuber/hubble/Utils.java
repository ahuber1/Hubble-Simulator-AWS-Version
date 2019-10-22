package ahuber.hubble;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class containing general purpose helper methods.
 */
public final class Utils {

    /**
     * Clamps the provided value in the specified range.
     * @param value The value to clamp.
     * @param inclusiveMin The inclusive lower bound of the range.
     * @param inclusiveMax The inclusive upper bound of the range.
     * @param <T> The type of value to clamp.
     * @return {@code value} clamped in the range [{@code inclusiveMin}, {@code inclusiveMax}].
     * @throws NullPointerException If {@code value}, {@code inclusiveMin}, or {@code inclusiveMax} are {@code null}.
     * @throws IllegalArgumentException If {@code inclusiveMax} &gt; {@code inclusiveMin}.
     */
    public static <T extends Comparable<T>> T clamp(@NotNull T value, @NotNull T inclusiveMin, @NotNull T inclusiveMax) {
        Objects.requireNonNull(value, "The value cannot be null");
        Objects.requireNonNull(inclusiveMin, "The inclusive min cannot be null.");
        Objects.requireNonNull(inclusiveMax, "The inclusive max cannot be null.");
        int minMaxCompareResult = inclusiveMin.compareTo(inclusiveMax);

        if (minMaxCompareResult > 0) {
            String message = String.format("Invalid combination of inclusive min and max. Inclusive Min: %s, " +
                    "Inclusive Max: %s", inclusiveMin, inclusiveMax);
            throw new IllegalArgumentException(message);
        }

        if (value.compareTo(inclusiveMin) < 0) {
            return inclusiveMin;
        }

        if (value.compareTo(inclusiveMax) > 0) {
            return inclusiveMax;
        }

        return value;
    }
}
