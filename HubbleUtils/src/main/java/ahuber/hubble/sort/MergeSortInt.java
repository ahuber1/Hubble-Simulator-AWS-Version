package ahuber.hubble.sort;

import ahuber.hubble.adt.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * A class containing methods for sorting {@code int} arrays.
 */
public final class MergeSortInt {

    /**
     * Sorts the provided array using a multi-threaded version of Merge Sort.
     * @param array The array to sort.
     * @param threshold The maximum length of a sub-array where the sub-array is sorted using insertion sort without
     *                  splitting the sub-array into two halves.
     * @throws IllegalArgumentException If {@code threshold} is not in the range 2 &le; {@code threshold} &le;
     * {@code array.length}.
     * @throws NullPointerException If {@code array} is {@code null}
     */
    public static void sort(@NotNull int[] array, int threshold) {
        Objects.requireNonNull(array, "The array cannot be null");

        if (threshold > array.length) {
            String message = String.format("threshold is greater than the array length. " +
                    "(threshold: %d, array length: %d)", threshold, array.length);
            throw new IllegalArgumentException(message);
        }

        ForkJoinPool pool = new ForkJoinPool();
        MergeSortAction action = new MergeSortAction(array, 0, array.length - 1, threshold);
        pool.submit(action).join();
    }

    /**
     * Merges two {@code int} arrays sorted in ascending order into one {@code int} array sorted in ascending order
     * @param array1 The first {@code int} array sorted in ascending order.
     * @param array2 THe second {@code int} array sorted in ascending order.
     * @return An {@code int} array containing the elements in the two {@code int} arrays sorted in ascending order.
     * @throws NullPointerException If either array is {@code null}
     */
    @NotNull
    public static int[] merge(int[] array1, int[] array2) {
        Objects.requireNonNull(array1, "array1 cannot be null");
        Objects.requireNonNull(array2, "array2 cannot be null");
        int[] combined = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, combined, 0, array1.length);
        System.arraycopy(array2, 0, combined, array1.length, array2.length);
        merge(combined, 0, array1.length, combined.length - 1);
        return combined;
    }

    /**
     * Merges two "subarrays" together
     * @param array the array to "merge"
     * @param startInclusive the smallest index of the "left subarray"
     * @param middle the largest index of the "left subarray"
     * @param endInclusive the largest index of the "right subarray"
     */
    private static void merge(int[] array, int startInclusive, int middle, int endInclusive) {

        // the sorted copy of this "subarray"
        int[] a = new int[endInclusive - startInclusive + 1];

        for(int i = 0, l = startInclusive, r = middle + 1; i < a.length; i++, l++, r++) {

            if(l == middle + 1) { // If all the elements in the left "subarray" have been processed
                a[i] = array[r];
                l--; // stops l from being incremented
            }
            else if(r == endInclusive + 1) { // If all the elements in the "right" subarray have been processed
                a[i] = array[l];
                r--; // stops r from being incremented
            }
            else if(array[l] < array[r]) {
                a[i] = array[l];
                r--; // stops r from being incremented
            }
            else {
                a[i] = array[r];
                l--; // stops l from being incremented
            }

        }

        // copy the contents of the copy array back into the original
        for(int i = 0, m = startInclusive; i < a.length; i++, m++)
            array[m] = a[i];
    }

    private static void insertionSort(@NotNull int[] array, int startInclusive, int endInclusive) {
        for (int i = startInclusive; i <= endInclusive; i++) {
            for (int j = i + 1; j <= endInclusive; j++) {
                if (array[i] <= array[j]) {
                    continue;
                }

                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    private static class MergeSortAction extends RecursiveAction {
        private final int[] array;
        private final int startInclusive;
        private final int endInclusive;
        private final int threshold;

        MergeSortAction(int[] array, int startInclusive, int endInclusive, int threshold) {
            this.array = array;
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int length = ArrayUtils.calculateLength(startInclusive, endInclusive, array.length);

            if (length < threshold) {
                insertionSort(array, startInclusive, endInclusive);
                return;
            }

            int middle = startInclusive + length / 2;
            MergeSortAction leftAction = new MergeSortAction(array, startInclusive, middle, threshold);
            MergeSortAction rightAction = new MergeSortAction(array, middle + 1, endInclusive, threshold);
            invokeAll(leftAction, rightAction);
            merge(array, startInclusive, middle, endInclusive);
        }
    }
}
