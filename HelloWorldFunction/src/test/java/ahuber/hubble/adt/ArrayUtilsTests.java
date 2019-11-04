package ahuber.hubble.adt;

import ahuber.hubble.testing.TestingUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class ArrayUtilsTests {
    private final Random random = new Random();

    @Test
    public void testCalculateLength() {
        Assert.assertEquals(0, ArrayUtils.calculateLength(0, 0, 0));
        Assert.assertEquals(1,ArrayUtils.calculateLength(0, 0, 1));
        Assert.assertEquals(10, ArrayUtils.calculateLength(0, 9, 10));

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(randomNegativeInt(), 9, 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(0, randomNegativeInt(), 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(0, 9, randomNegativeInt()),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(9, 0, 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(10, 9, 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(Integer.MAX_VALUE, 9, 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(0, 10, 10),
                IndexOutOfBoundsException.class);

        TestingUtilities.assertExceptionThrown(() -> ArrayUtils.calculateLength(0, Integer.MAX_VALUE, 10),
                IndexOutOfBoundsException.class);
    }

    @Test
    public void testSwap() {
        Integer[] start = {1, 2};
        Integer[] end = {2, 1};
        ArrayUtils.swap(start, 0, 1);
        Assert.assertArrayEquals(end, start);
    }

    @Test
    public void testShift() {
        Integer[] numbers = IntStream.rangeClosed(0, 9).boxed().toArray(Integer[]::new);
        Integer[] array = Arrays.copyOf(numbers, numbers.length + 1);
        Integer[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, null};
        Assert.assertArrayEquals(expected, array);
        ArrayUtils.shiftRight(array, 1);
        Integer[] shiftedExpected = {null, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Assert.assertArrayEquals(shiftedExpected, array);
        ArrayUtils.shiftLeft(array, 1);
        Assert.assertArrayEquals(expected, array);
    }

    @Test
    public void testRevRange() {
        IntStream actualRevRangeStream = ArrayUtils.revRange(0, 5);
        IntStream actualRevRangeClosedStream = ArrayUtils.revRangeClosed(0, 5);

        int[] actualRevRangeArray = actualRevRangeStream.toArray();
        int[] actualRevRangeClosedArray = actualRevRangeClosedStream.toArray();

        int[] expectedRevRangeArray = {4, 3, 2, 1, 0};
        int[] expectedRevRangeClosedArray = {5, 4, 3, 2, 1, 0};

        Assert.assertArrayEquals(expectedRevRangeArray, actualRevRangeArray);
        Assert.assertArrayEquals(expectedRevRangeClosedArray, actualRevRangeClosedArray);
    }

    @Test
    public void testCombine() {
        int[] array1 = IntStream.range(0, 10).toArray();
        int[] array2 = IntStream.range(10, 20).toArray();
        int[] expected = IntStream.range(0, 20).toArray();
        int[] actual = ArrayUtils.combine(array1, array2);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testCombine2() {
        String[] array1 = IntStream.range(0, 10).mapToObj(Integer::toString).toArray(String[]::new);
        String[] array2 = IntStream.range(10, 20).mapToObj(Integer::toString).toArray(String[]::new);
        String[] expected = IntStream.range(0, 20).mapToObj(Integer::toString).toArray(String[]::new);
        String[] actual = ArrayUtils.combine(array1, array2, String[]::new);
        Assert.assertArrayEquals(expected, actual);
    }

    private int randomNegativeInt() {
        return -Math.abs(random.nextInt());
    }
}
