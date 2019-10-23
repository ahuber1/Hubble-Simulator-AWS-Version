package ahuber.hubble;

import ahuber.hubble.adt.IntArrayWrapper;
import ahuber.hubble.sort.MergeSort;
import ahuber.hubble.sort.MergeSortInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class SatelliteProcessor implements Processor<IntArrayWrapper>, Runnable {
    private final int threshold;
    private final AtomicReference<@Nullable BufferedImage> image = new AtomicReference<>();
    private final Semaphore semaphore = new Semaphore(1);

    public SatelliteProcessor(int threshold) {
        this.threshold = threshold;
        semaphore.acquireUninterruptibly();
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            semaphore.release();
        } catch (InterruptedException ignored) {
            semaphore.release();
        }
    }

    @Nullable
    public synchronized BufferedImage getImage() {
        return image.get();
    }

    @Override
    public synchronized void onReceived(@NotNull IntArrayWrapper wrapper) {
        Thread thread = new Thread(() -> {
            int[] array = wrapper.getArray();
            MergeSortInt.sort(array, threshold);
            BufferedImage image = SatelliteImageWriter.writeGreyscaleImage(array);
            this.image.set(image);
        });
        thread.start();
    }
}
