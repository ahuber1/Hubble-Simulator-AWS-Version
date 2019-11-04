package ahuber.hubble.sort;

import ahuber.hubble.adt.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

public class MergeSortTests {

    @Test
    public void testMergeSort() throws InterruptedException {
        Integer[] actual = ArrayUtils.revRange(0, 100).boxed().toArray(Integer[]::new);
        Integer[] expected = IntStream.range(0, 100).boxed().toArray(Integer[]::new);
        MergeSort.sort(actual, 10);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testMergeSortInt() {
        int[] actual = ArrayUtils.revRange(0, 100).toArray();
        int[] expected = IntStream.range(0, 100).toArray();
        MergeSortInt.sort(actual, 10);
        Assert.assertArrayEquals(expected, actual);
    }
}
