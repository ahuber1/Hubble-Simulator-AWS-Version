package ahuber.hubble.sort;

import ahuber.hubble.adt.ArrayUtils;
import ahuber.hubble.adt.ArrayWrapper;
import ahuber.hubble.adt.StandardArrayWrapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

/**
 * A class containing methods for sorting generic arrays using Merge Sort.
 */
public final class MergeSort {

    /**
     * Sorts the provided array using a multi-threaded version of Merge Sort.
     * @param array The array to sort.
     * @param threshold The maximum length of a sub-array where the sub-array is sorted using insertion sort without
     *                  splitting the sub-array in two halves.
     * @param <T> The type of data to sort.
     * @throws InterruptedException If a thread is interrupted.
     * @throws IllegalArgumentException If {@code threshold} is not in the range 2 &le; {@code threshold} &le;
     * {@code array.length}
     * @throws NullPointerException If {@code array} is {@code null}
     */
    public static <T extends Comparable<T>> void sort(@NotNull T[] array, int threshold) throws InterruptedException {
        sort(new StandardArrayWrapper<>(Objects.requireNonNull(array, "The array cannot be null")), threshold);
    }

    /**
     * Sorts an array contained within the provided {@link ArrayWrapper} using a multi-threaded version of Merge Sort.
     * @param wrapper The {@link ArrayWrapper}
     * @param threshold The maximum length of a sub-array where the sub-array is sorted using insertion sort without
     *                  splitting the sub-array into two halves.
     * @param <T> The type of data to sort.
     * @throws InterruptedException If a thread is interrupted.
     * @throws IllegalArgumentException If {@code threshold} is not in the range 2 &le; {@code threshold} &le;
     * {@link ArrayWrapper#length()}
     * @throws NullPointerException If {@code array} is {@code null}
     */
    public static <T extends Comparable<T>> void sort(@NotNull ArrayWrapper<T> wrapper, int threshold) throws InterruptedException {
        Objects.requireNonNull(wrapper, "The wrapper cannot be null.");

        if (threshold < 2) {
            String message = String.format("The threshold cannot be less than 2. (threshold: %d)", threshold);
            throw new IllegalArgumentException(message);
        }

        if (threshold > wrapper.length()) {
            String message = String.format("threshold is greater than the array length. " +
                    "(threshold: %d, array length: %d)", threshold, wrapper.length());
            throw new IllegalArgumentException(message);
        }

        sort(wrapper, 0, wrapper.length() - 1, threshold);
    }

    private static <T extends Comparable<T>> void sort(@NotNull ArrayWrapper<T> wrapper, int startInclusive, int endInclusive,
            int threshold) throws InterruptedException {

        int length = ArrayUtils.calculateLength(startInclusive, endInclusive, wrapper.length());

        if (length >= threshold) {
            try {
                int middle = startInclusive + length / 2;
                Thread leftHalfThread = new Thread(() -> {
                    try {
                        sort(wrapper, startInclusive, middle, threshold);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                Thread rightHalfThread = new Thread(() -> {
                    try {
                        sort(wrapper, middle + 1, endInclusive, threshold);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                leftHalfThread.start();
                rightHalfThread.start();

                leftHalfThread.join();
                rightHalfThread.join();
            } catch (RuntimeException exception) {

                if (exception.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) exception.getCause();
                }

                throw exception;
            }
        }

        insertionSort(wrapper, startInclusive, endInclusive);
    }

    private static <T extends Comparable<T>> void insertionSort(@NotNull ArrayWrapper<T> wrapper, int startInclusive,
            int endInclusive) {
        Comparator<T> comparator = getComparator();

        for (int i = startInclusive; i <= endInclusive; i++) {
            for (int j = i + 1; j <= endInclusive; j++) {
                int compareResult = comparator.compare(wrapper.get(i), wrapper.get(j));

                // wrapper[i] > wrapper[j]
                if (compareResult > 0) {
                    wrapper.swap(i, j);
                }
            }
        }
    }

    @Contract(pure = true)
    @NotNull
    private static <T extends Comparable<T>> Comparator<T> getComparator() {
        return Comparator.nullsLast(Comparable::compareTo);
    }
}