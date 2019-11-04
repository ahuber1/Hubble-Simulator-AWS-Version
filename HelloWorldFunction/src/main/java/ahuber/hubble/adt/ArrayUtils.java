package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * A class containing various utilities for arrays.
 */
public final class ArrayUtils {
    /**
     * Calculates the length of a sub-array specified start and end indices.
     * @param startIndex The start index (inclusive)
     * @param endIndex The end index (inclusive)
     * @param arrayLength The parent array's length.
     * @return The length of the specified sub-array.
     * @throws ArrayIndexOutOfBoundsException If there is an illegal combination of indices and array length.
     */
    public static int calculateLength(int startIndex, int endIndex, int arrayLength) {
        Lazy<String> messageLazy = new Lazy<>(() -> String.format("Arguments: [startIndex = %d, endIndex = %d, " +
                "arrayLength = %d]", startIndex, endIndex, arrayLength));

        if (startIndex < 0) {
            String message = String.format("startIndex < 0 (%s)", messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (endIndex < 0) {
            String message = String.format("endIndex < 0 (%s)", messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (arrayLength < 0) {
            String message = String.format("arrayLength < 0 (%s)", messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (endIndex < startIndex) {
            String message = String.format("endIndex < startIndex (%s)", messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (startIndex == 0 && endIndex == 0 && arrayLength == 0) {
            return 0;
        }

        if (startIndex >= arrayLength) {
            String message = String.format("startIndex >= arrayLength && arrayLength > 0 (%s)",
                    messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (endIndex >= arrayLength) {
            String message = String.format("endIndex >= arrayLength && arrayLength > 0 (%s)", messageLazy.getValue());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        return endIndex - startIndex + 1;
    }

    /**
     * Swaps two elements specified by the two indices in the provided array.
     * @param array The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @param <T> The type of items in the array
     * @throws NullPointerException If {@code array} is {@code null}
     * @throws IndexOutOfBoundsException If {@code index1} or {@code index2} is outside the bounds of {@code array}
     */
    @Contract(mutates = "param1")
    public static <T> void swap(@NotNull T[] array, int index1, int index2) {
        Objects.requireNonNull(array, "Array cannot be null.");
        T item = array[index1];
        array[index1] = array[index2];
        array[index2] = item;
    }

    /**
     * Shifts the array to the right by the provided amount. This array runs in O(<i>sn</i>) time where <i>n</i> is
     * the number of items in the array and <i>s</i> is the number of times we shift to the right (i.e., the value of
     * {@code amount}).
     * @param array The array to shift.
     * @param amount The amount of times the items in the array are shifted.
     * @param <T> The types of items in the array.
     * @throws IllegalArgumentException If {@code amount} &lt; 0.
     * @see #shiftLeft(Object[], int)
     */
    public static <T> void shiftRight(T[] array, int amount) {
        if (amount < 0) {
            String message = String.format("amount < 0 (amount: %d)", amount);
            throw new IllegalArgumentException(message);
        }

        while (amount > 0 && array.length > 0) {
            for (int i = 1; i < array.length; i++) {
                swap(array, 0, i);
            }

            --amount;
        }
    }

    /**
     * Shifts the array to the left by the provided amount. This array runs in O(<i>sn</i>) time where <i>n</i> is
     * the number of items in the array and <i>s</i> is the number of times we shift to the right (i.e., the value of
     * {@code amount}).
     * @param array The array to shift.
     * @param amount The amount of times the items in the array are shifted.
     * @param <T> The types of items in the array.
     * @throws IllegalArgumentException If {@code amount} &lt; 0.
     * @see #shiftRight(Object[], int)
     */
    public static <T> void shiftLeft(T[] array, int amount) {
        if (amount < 0) {
            String message = String.format("amount < 0 (amount: %d)", amount);
            throw new IllegalArgumentException(message);
        }

        int lastIndex = array.length - 1;

        while (amount > 0 && array.length > 0) {
            for (int i = lastIndex - 1; i >= 0; i--) {
                swap(array, lastIndex, i);
            }

            --amount;
        }
    }

    /**
     * Returns a reversed sequential ordered {@link IntStream} from {@code endExclusive} (exclusive) to
     * {@code startInclusive } (inclusive) by an incremental step of 1.
     * @param startInclusive The (inclusive) value at the end of the sequence
     * @param endExclusive The exclusive value at the start of the sequence.
     * @return A sequential {@link IntStream} for the range of {@code int} elements.
     */
    public static IntStream revRange(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .map(i -> endExclusive - i + startInclusive - 1);
    }

    /**
     * Returns a reversed sequential ordered {@link IntStream} from {@code endInclusive} (inclusive) to
     * {@code startInclusive} (inclusive) by an incremental step of 1.
     * @param startInclusive The (inclusive) value at the end of the sequence.
     * @param endInclusive The inclusive value at the start of the sequence.
     * @return A sequential {@link IntStream} for the range of {@code int} elements.
     */
    public static IntStream revRangeClosed(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .map(i -> endInclusive - i + startInclusive);
    }

    @NotNull
    public static <T> T[] combine(T[] array1, T[] array2, IntFunction<T[]> generator) {
        return combine(new StandardArrayWrapper<>(Objects.requireNonNull(array1, "'array1' cannot be null.")),
                new StandardArrayWrapper<>(Objects.requireNonNull(array2, "'array2' cannot be null.")),
                length -> new StandardArrayWrapper<>(generator.apply(length))).getArray();
    }

    @NotNull
    public static int[] combine(int[] array1, int[] array2) {
        return combine(new IntArrayWrapper(Objects.requireNonNull(array1, "'array1' cannot be null.")),
                new IntArrayWrapper(Objects.requireNonNull(array2, "'array2' cannot be null.")),
                length -> new IntArrayWrapper(new int[length])).getArray();
    }

    private static <W extends ArrayWrapper<T>, T> W combine(W wrapper1, W wrapper2, IntFunction<W> generator) {
        Objects.requireNonNull(wrapper1, "'wrapper1' cannot be null.");
        Objects.requireNonNull(wrapper2, "'wrapper2' cannot be null.");
        Objects.requireNonNull(generator, "'generator' cannot be null.");

        int length = wrapper1.length() + wrapper2.length();
        W wrapper = generator.apply(length);
        Objects.requireNonNull(wrapper, "'generator' cannot return null.");

        for (int i = 0; i < wrapper1.length(); i++) {
            wrapper.set(i, wrapper1.get(i));
        }

        for (int i = 0; i < wrapper2.length(); i++) {
            wrapper.set(i + wrapper1.length(), wrapper2.get(i));
        }

        return wrapper;
    }
    
}
