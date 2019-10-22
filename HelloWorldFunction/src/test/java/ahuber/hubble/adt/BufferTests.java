package ahuber.hubble.adt;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(DataProviderRunner.class)
public class BufferTests implements SizeObserver<Buffer<Integer>> {

    // region Main Test

    private static final int CAPACITY = 10;
    private boolean observerActive;
    private int expectedSize;
    private boolean observerCalled;

    @NotNull
    @Contract(" -> new")
    @DataProvider
    public static Object[][] dataProviderIntegerBuffer() {
        return new Object[][] {
                {new Buffer<>(new StandardArrayWrapper<>(new Integer[CAPACITY])), "Integer Buffer"},
                {new Buffer<>(new IntArrayWrapper(new int[CAPACITY])), "Int Buffer"}
        };
    }

    @Test
    @UseDataProvider("dataProviderIntegerBuffer")
    public void testAddAndRemove(@NotNull Buffer<Integer> buffer, @NotNull String description) {
        System.out.println(description);
        buffer.registerObserver(this);
        List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());

        // Create a list of all possible consecutive sub lists of "list"
        List<List<Integer>> consecutiveSlices = getAllConsecutiveSlices(list);

        testEmpty(buffer, list, consecutiveSlices);
        testAdd(buffer, list);

        Assert.assertTrue(buffer.isFull());
        Assert.assertEquals(CAPACITY, buffer.capacity());
        Assert.assertFalse(buffer.isEmpty());
        Assert.assertTrue(buffer.containsAll(list));
        Assert.assertTrue(consecutiveSlices.stream().allMatch(buffer::containsAll));

        Integer[] expected = IntStream.range(0, 10).boxed().toArray(Integer[]::new);
        Assert.assertFalse(buffer.add(11));
        Assert.assertArrayEquals(expected, buffer.toArray(new Integer[0]));

        Assert.assertFalse(buffer.addAll(IntStream.of(1, 2, 3, 4, 5).boxed().collect(Collectors.toList())));
        Assert.assertArrayEquals(expected, buffer.toArray(new Integer[0]));

        Assert.assertFalse(buffer.retainAll(list));
        Assert.assertArrayEquals(expected, buffer.toArray(new Integer[0]));

        int[] itemsToRemove = {5, 6, 7, 8, 9, 0, 1, 2, 3, 4};
        List<Integer> listCopy = new ArrayList<>(list);

        testRemove(buffer, list, itemsToRemove, listCopy);

        Integer[] itemsToAdd = Arrays.stream(itemsToRemove).boxed().toArray(Integer[]::new);
        itemsToAdd = Arrays.copyOf(itemsToAdd, itemsToAdd.length - 1);
        Integer[] expectedIntegers = Arrays.copyOf(itemsToAdd, itemsToAdd.length + 1);
        ArrayUtils.shiftRight(expectedIntegers, 1);
        expectedIntegers[0] = itemsToRemove[itemsToRemove.length - 1];
        Object[] expectedObjects = Arrays.stream(expectedIntegers).toArray();

        observerActive = true;
        observerCalled = false;
        expectedSize = CAPACITY;
        Assert.assertTrue(buffer.addAll(Arrays.stream(itemsToAdd).collect(Collectors.toList())));
        Assert.assertTrue(observerCalled);
        Integer[] actualIntegers = buffer.toArray(new Integer[0]);
        Object[] actualObjects = buffer.toArray();
        Assert.assertArrayEquals(expectedIntegers, actualIntegers);
        Assert.assertArrayEquals(expectedObjects, actualObjects);
        Assert.assertEquals(CAPACITY, buffer.capacity());
        Assert.assertTrue(buffer.isFull());
        Assert.assertEquals(CAPACITY, buffer.size());

        observerCalled = false;
        expectedSize = 0;
        Assert.assertTrue(buffer.removeAll(Arrays.stream(expectedIntegers).collect(Collectors.toList())));
        testEmpty(buffer, list, consecutiveSlices);
        Assert.assertTrue(buffer.unregisterObserver(this));
    }

    private void testRemove(Buffer<Integer> buffer, List<Integer> list, @NotNull int[] itemsToRemove, List<Integer> listCopy) {
        Object[] expectedObjects = null;
        Integer[] expectedIntegers = null;
        Object[] actualObjects = null;
        Integer[] actualIntegers = null;

        for (int i = 0; i < itemsToRemove.length - 1; i++) {
            int item = itemsToRemove[i];
            observerActive = true;
            observerCalled = false;
            --expectedSize;
            Assert.assertTrue(buffer.remove(item));
            Assert.assertTrue(observerCalled);
            Assert.assertTrue(listCopy.remove((Integer) item));
            Assert.assertTrue(listCopy.containsAll(buffer));

            observerActive = false;
            expectedObjects = listCopy.toArray();
            expectedIntegers = listCopy.toArray(new Integer[0]);
            actualObjects = buffer.toArray();
            actualIntegers = buffer.toArray(new Integer[0]);
            Assert.assertArrayEquals(expectedIntegers, actualIntegers);
            Assert.assertArrayEquals(expectedObjects, actualObjects);

            Assert.assertEquals(CAPACITY, buffer.capacity());
            Assert.assertFalse(buffer.isFull());
            Assert.assertEquals(expectedSize, buffer.size());
        }

        Assert.assertFalse(buffer.addAll(list));
        Assert.assertArrayEquals(expectedIntegers, actualIntegers);
        Assert.assertArrayEquals(expectedObjects, actualObjects);
    }

    @SuppressWarnings("RedundantOperationOnEmptyContainer")
    private void testEmpty(@NotNull Buffer<Integer> buffer, @NotNull List<Integer> list, @NotNull List<List<Integer>> consecutiveSlices) {
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertFalse(buffer.remove(1));
        Assert.assertTrue(list.stream().noneMatch(buffer::contains));
        Assert.assertTrue(consecutiveSlices.stream().noneMatch(buffer::containsAll));
        Assert.assertTrue(consecutiveSlices.stream().noneMatch(buffer::removeAll));
        Assert.assertTrue(consecutiveSlices.stream().noneMatch(buffer::retainAll));
    }

    private void testAdd(Buffer<Integer> buffer, @NotNull List<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            observerActive = false;
            Integer[] expectedIntegers = IntStream.range(0, i).boxed().toArray(Integer[]::new);
            Object[] expectedObjects = Arrays.stream(expectedIntegers).toArray();

            Assert.assertEquals(CAPACITY, buffer.capacity());
            Assert.assertFalse(buffer.isFull());
            Assert.assertEquals(expectedSize, buffer.size());
            Assert.assertArrayEquals(expectedIntegers, buffer.toArray(new Integer[0]));
            Assert.assertArrayEquals(expectedObjects, buffer.toArray());

            observerActive = true;
            observerCalled = false;
            ++expectedSize;
            Assert.assertTrue(buffer.add(i));
            Assert.assertTrue(observerCalled);

            expectedIntegers = IntStream.rangeClosed(0, i).boxed().toArray(Integer[]::new);
            expectedObjects = Arrays.stream(expectedIntegers).toArray();

            Assert.assertEquals(expectedSize, buffer.size());
            Assert.assertArrayEquals(expectedIntegers, buffer.toArray(new Integer[0]));
            Assert.assertArrayEquals(expectedObjects, buffer.toArray());
        }

        observerActive = false;
        observerCalled = true;
    }

    @NotNull
    private static <T> List<List<T>> getAllConsecutiveSlices(@NotNull List<T> collection) {
        List<List<T>> outerList = new ArrayList<>();

        for (int i = 0; i < collection.size(); i++) {
            for (int j = i; j < collection.size(); j++) {
                List<T> innerList = new ArrayList<>();

                for (int k = i; k <= j; k++) {
                    innerList.add(collection.get(k));
                }

                outerList.add(innerList);
            }
        }

        return outerList;
    }

    @Override
    public void sizeChanged(Buffer<Integer> collection) {
        if (!observerActive) {
            Assert.fail("Observer should not be active.");
        }

        if (observerCalled) {
            Assert.fail("observerCalled == true");
        }

        Assert.assertEquals(expectedSize, collection.size());
        observerCalled = true;
    }

    // endregion

    @SuppressWarnings("ConstantConditions")
    @Test
    @UseDataProvider("dataProviderIntegerBuffer")
    public void testOverflow(@NotNull Buffer<Integer> buffer, @NotNull String description) {
        System.out.println(description);
        AtomicInteger callbackSize = new AtomicInteger();
        buffer.registerObserver(collection -> callbackSize.set(collection.size()));
        Assert.assertTrue(buffer.addAll(IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList()), false));
        Assert.assertEquals(CAPACITY, buffer.size());
        List<Integer> expected = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
        Assert.assertTrue(buffer.containsAll(expected));
        Assert.assertTrue(buffer.stream().allMatch(number -> number < 11 && number > 0));
        Assert.assertArrayEquals(expected.toArray(new Integer[0]), buffer.toArray(new Integer[0]));
        Assert.assertArrayEquals(expected.toArray(), buffer.toArray());
        Assert.assertTrue(buffer.isFull());

        expected = IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList());
        Assert.assertTrue(buffer.retainAll(expected));
        Assert.assertEquals(CAPACITY / 2, buffer.size());
        Assert.assertTrue(buffer.containsAll(expected));
        Assert.assertTrue(buffer.stream().allMatch(number -> number < 6 && number > 0));
        Assert.assertArrayEquals(expected.toArray(new Integer[0]), buffer.toArray(new Integer[0]));
        Assert.assertArrayEquals(expected.toArray(), buffer.toArray());

        Assert.assertTrue(buffer.addAll(IntStream.rangeClosed(6, 10).boxed().collect(Collectors.toList()), false));
        Assert.assertEquals(CAPACITY, buffer.size());
        expected = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
        Assert.assertTrue(buffer.containsAll(expected));
        Assert.assertTrue(buffer.stream().allMatch(number -> number < 11 && number > 0));
        Assert.assertArrayEquals(expected.toArray(new Integer[0]), buffer.toArray(new Integer[0]));
        Assert.assertArrayEquals(expected.toArray(), buffer.toArray());
        Assert.assertTrue(buffer.isFull());

        Assert.assertFalse(buffer.containsAll(new ArrayList<>()));
        buffer.clear();
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());
        Assert.assertEquals(0, callbackSize.get());
        Assert.assertEquals(CAPACITY, buffer.capacity());
        Assert.assertFalse(buffer.isFull());
        Assert.assertFalse(buffer.containsAll(expected));
        Assert.assertTrue(buffer.containsAll(new ArrayList<>()));
    }

    @Test
    public void testTakeGeneric() {
        AtomicInteger size = new AtomicInteger();
        Buffer<Integer> buffer = new Buffer<>(new Integer[CAPACITY]);
        buffer.registerObserver(collection -> size.set(collection.size()));
        buffer.addAll(IntStream.range(0, CAPACITY).boxed().collect(Collectors.toList()));
        Assert.assertArrayEquals(IntStream.range(0, CAPACITY).boxed().toArray(), buffer.toArray(new Integer[0]));
        Assert.assertEquals(CAPACITY, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());

        Integer[] actualFirstHalf = buffer.take(CAPACITY / 2, new Integer[0]);
        Integer[] expectedFirstHalf = IntStream.range(0, CAPACITY / 2).boxed().toArray(Integer[]::new);
        Assert.assertArrayEquals(expectedFirstHalf, actualFirstHalf);
        Assert.assertEquals(CAPACITY / 2, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());

        Integer[] actualSecondHalf = buffer.toArray(new Integer[0]);
        Integer[] expectedSecondHalf = IntStream.range(CAPACITY / 2, CAPACITY).boxed().toArray(Integer[]::new);
        Assert.assertArrayEquals(expectedSecondHalf, actualSecondHalf);

        buffer.addAll(Arrays.stream(actualFirstHalf).collect(Collectors.toList()));
        Integer[] expectedEndResult = Stream.concat(Arrays.stream(expectedSecondHalf),
                Arrays.stream(expectedFirstHalf)).toArray(Integer[]::new);
        Integer[] actualEndResult = buffer.toArray(new Integer[0]);
        Assert.assertArrayEquals(expectedEndResult, actualEndResult);
        Assert.assertEquals(CAPACITY, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());
    }

    @Test
    public void testTakeNonGeneric() {
        AtomicInteger size = new AtomicInteger();
        IntBuffer buffer = new IntBuffer(new IntArrayWrapper(new int[CAPACITY]));
        buffer.registerObserver(collection -> size.set(collection.size()));
        buffer.addAll(IntStream.range(0, CAPACITY).toArray());
        Assert.assertArrayEquals(IntStream.range(0, CAPACITY).toArray(), buffer.toIntArray());
        Assert.assertEquals(CAPACITY, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());

        int[] actualFirstHalf = buffer.take(CAPACITY / 2);
        int[] expectedFirstHalf = IntStream.range(0, CAPACITY / 2).toArray();
        Assert.assertArrayEquals(expectedFirstHalf, actualFirstHalf);
        Assert.assertEquals(CAPACITY / 2, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());

        int[] actualSecondHalf = buffer.toIntArray();
        int[] expectedSecondHalf = IntStream.range(CAPACITY / 2, CAPACITY).toArray();
        Assert.assertArrayEquals(expectedSecondHalf, actualSecondHalf);

        buffer.addAll(actualFirstHalf);
        int[] expectedEndResult =
                IntStream.concat(IntStream.of(expectedSecondHalf), IntStream.of(expectedFirstHalf)).toArray();
        int[] actualEndResult = buffer.toIntArray();
        Assert.assertArrayEquals(expectedEndResult, actualEndResult);
        Assert.assertEquals(CAPACITY, buffer.size());
        Assert.assertEquals(buffer.size(), size.get());
    }
}
