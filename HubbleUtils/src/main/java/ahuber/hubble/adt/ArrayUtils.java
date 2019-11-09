package ahuber.hubble.adt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class containing various utilities for arrays.
 */
public final class ArrayUtils {
    /**
     * Calculates the length of a sub-array specified start and end indices.
     *
     * @param startIndex  The start index (inclusive)
     * @param endIndex    The end index (inclusive)
     * @param arrayLength The parent array's length.
     * @return The length of the specified sub-array.
     * @throws ArrayIndexOutOfBoundsException If there is an illegal combination of indices and array length.
     */
    public static int calculateLength(int startIndex, int endIndex, int arrayLength) {

        Supplier<String> argumentDescriptionSupplier = () -> String.format("Arguments: [startIndex = %d, " +
                "endIndex = %d, arrayLength = %d]", startIndex, endIndex, arrayLength);

        // -------------------------------------------------------------
        // Check if any of the indices or the array length are negative
        // -------------------------------------------------------------
        if (startIndex < 0) {
            String message = String.format("startIndex < 0 (%s)", argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (endIndex < 0) {
            String message = String.format("endIndex < 0 (%s)", argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (arrayLength < 0) {
            String message = String.format("arrayLength < 0 (%s)", argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        // ----------------------------------------
        // Ensure startIndex comes before endIndex
        // ----------------------------------------
        if (endIndex < startIndex) {
            String message = String.format("endIndex < startIndex (%s)", argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        // -----------------------------------
        // Make sure we handle this edge case
        // -----------------------------------
        if (startIndex == 0 && endIndex == 0 && arrayLength == 0) {
            return 0;
        }

        // ---------------------------------------------------------------------------
        // Ensure the indices do not go outside the bounds of the length of the array
        // ---------------------------------------------------------------------------
        if (startIndex >= arrayLength) {
            String message = String.format("startIndex >= arrayLength && arrayLength > 0 (%s)",
                    argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        if (endIndex >= arrayLength) {
            String message = String.format("endIndex >= arrayLength && arrayLength > 0 (%s)",
                    argumentDescriptionSupplier.get());
            throw new ArrayIndexOutOfBoundsException(message);
        }

        // -----------------------------------------------------------------------------------------------------------
        // Now that we've checked the indices and array length, let's calculate the length of the specified sub-array
        // -----------------------------------------------------------------------------------------------------------
        return endIndex - startIndex + 1;
    }

    /**
     * Swaps two elements specified by the two indices in the provided array.
     *
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @param <T>    The type of items in the array
     * @throws NullPointerException      If {@code array} is {@code null}
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
     *
     * @param array  The array to shift.
     * @param amount The amount of times the items in the array are shifted.
     * @param <T>    The types of items in the array.
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
     *
     * @param array  The array to shift.
     * @param amount The amount of times the items in the array are shifted.
     * @param <T>    The types of items in the array.
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
     *
     * @param startInclusive The (inclusive) value at the end of the sequence
     * @param endExclusive   The exclusive value at the start of the sequence.
     * @return A sequential {@link IntStream} for the range of {@code int} elements.
     */
    public static IntStream revRange(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .map(i -> endExclusive - i + startInclusive - 1);
    }

    /**
     * Returns a reversed sequential ordered {@link IntStream} from {@code endInclusive} (inclusive) to
     * {@code startInclusive} (inclusive) by an incremental step of 1.
     *
     * @param startInclusive The (inclusive) value at the end of the sequence.
     * @param endInclusive   The inclusive value at the start of the sequence.
     * @return A sequential {@link IntStream} for the range of {@code int} elements.
     * @implNote If {@code endInclusive} &lt; {@code startInclusive}, then the returned {@link IntStream} will be empty.
     */
    public static IntStream revRangeClosed(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .map(i -> endInclusive - i + startInclusive);
    }

    /**
     * Combines the specified arrays into one array.
     *
     * @param generator An {@link IntFunction} that generates and returns an array of type {@code T} of the specified
     *                  size.
     * @param arrays    The arrays to combine.
     * @param <T>       The type of objects stored in each array.
     * @return The combined arrays or an empty array if {@code arrays} is {@code null}
     * @throws NullPointerException If {@code generator} is {@code null} or returns {@code null}
     * @implNote Any array in {@code arrays} that is {@code null} is skipped and is not included in the returned array.
     */
    @SafeVarargs
    @NotNull
    @Contract("null, _ -> fail")
    public static <T> T[] combine(IntFunction<T[]> generator, T[]... arrays) {
        Objects.requireNonNull(generator, "'generator' cannot be null.");

        if (arrays == null) {
            return generator.apply(0);
        }

        List<StandardArrayWrapper<T>> list = Arrays.stream(arrays)
                .filter(Objects::nonNull)
                .map(StandardArrayWrapper::new)
                .collect(Collectors.toList());

        StandardArrayWrapper<T> combinedWrapper =
                combine(list, len -> new StandardArrayWrapper<>(generator.apply(len)));

        return combinedWrapper.getArray();
    }

    /**
     * Combines the specified int arrays into one array.
     *
     * @param arrays The int arrays to combine
     * @return The combined arrays or an empty array if {@code arrays} is {@code null}
     * @implNote Any array in {@code arrays} that is {@code null} is skipped and is not included in the returned array.
     */
    @NotNull
    public static int[] combine(int[]... arrays) {
        if (arrays == null) {
            return new int[0];
        }

        IntArrayWrapper[] wrappers = Arrays.stream(arrays)
                .filter(Objects::nonNull)
                .map(IntArrayWrapper::new)
                .toArray(IntArrayWrapper[]::new);

        IntArrayWrapper combinedWrapper = combine(IntArrayWrapper::new, wrappers);
        return combinedWrapper.getArray();
    }

    /**
     * Combines the provided {@link ArrayWrapper} into one array wrapper.
     *
     * @param generator  A function that returns an empty {@link ArrayWrapper} with the specified length.
     * @param wrappers   The {@link ArrayWrapper}s to combine.
     * @param <TWrapper> The {@link ArrayWrapper} type.
     * @param <T>        The type of elements in the {@link ArrayWrapper}
     * @return The combined {@link ArrayWrapper}s aor an empty {@link ArrayWrapper} if {@code wrappers} is {@code null}
     * @throws NullPointerException If {@code generator} is {@code null} or returns {@code null}
     * @implNote Any {@link ArrayWrapper} in {@code wrappers} that is {@code null} is skipped and is not included in
     * the returned {@link ArrayWrapper}.
     */
    @Contract("null, _ -> fail")
    @SuppressWarnings("WeakerAccess")
    @SafeVarargs
    public static <TWrapper extends ArrayWrapper<T>, T> TWrapper combine(IntFunction<TWrapper> generator,
            TWrapper... wrappers) {
        Objects.requireNonNull(generator, "'generator' cannot return null.");

        if (wrappers == null) {
            return generator.apply(0);
        }

        return combine(Arrays.asList(wrappers), generator);
    }

    /**
     * Combines the provided collection of {@link ArrayWrapper}s into one {@link ArrayWrapper}
     *
     * @param wrappers   The {@link ArrayWrapper}s
     * @param generator  A function that returns an empty {@link ArrayWrapper} of the specified size.
     * @param <TWrapper> The {@link ArrayWrapper} type.
     * @param <T>        The type of elements in the {@link ArrayWrapper}
     * @return The combined {@link ArrayWrapper}s or an empty {@link ArrayWrapper} if {@code wrappers} is {@code null}
     * @throws NullPointerException If {@code generator} is {@code null} or returns {@code null}
     * @implNote Any {@link ArrayWrapper} in {@code wrappers} that is {@code null} is skipped and is not included in
     * the returned {@link ArrayWrapper}.
     */
    @Contract("_, null -> fail")
    @NotNull
    private static <TWrapper extends ArrayWrapper<T>, T> TWrapper combine(Collection<TWrapper> wrappers,
            IntFunction<TWrapper> generator) {
        int length = 0;

        if (wrappers != null) {
            length = wrappers.stream().filter(Objects::nonNull).mapToInt(TWrapper::length).sum();
        }

        TWrapper wrapper = Objects.requireNonNull(generator, "'generator' cannot be null.").apply(length);
        Objects.requireNonNull(generator, "'generator' cannot return null.");

        if (length == 0) {
            return wrapper;
        }

        int offset = 0;

        for (TWrapper subWrapper : wrappers) {
            if (subWrapper == null) {
                continue;
            }

            for (int i = 0; i < subWrapper.length(); i++) {
                wrapper.set(i + offset, subWrapper.get(i));
            }

            offset += subWrapper.length();
        }

        return wrapper;
    }

}