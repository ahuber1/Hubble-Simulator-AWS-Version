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
        for (int i = 10; i < 10_000; i++) {
            System.out.printf("i = %d\n", i);
            int[] actual = ArrayUtils.revRange(0, i).toArray();
            int[] expected = IntStream.range(0, i).toArray();
            MergeSortInt.sort(actual, 10);
            Assert.assertArrayEquals(expected, actual);
        }
    }
}
