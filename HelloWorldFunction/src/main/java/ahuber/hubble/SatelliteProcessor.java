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
            System.out.println("Data received!");
            System.out.println("Sorting...START");
            MergeSortInt.sort(wrapper.getArray(), threshold);
            System.out.println("Sorting...DONE");

            System.out.println("Normalizing...START");
            byte[] normalizedData = new byte[wrapper.length()];

            for (int i = 0; i < wrapper.length(); i++) {
                normalizedData[i] = normalize(wrapper.get(i));
            }

            System.out.println("Normalizing...END");
            System.out.println("Writing image...START");
            BufferedImage image = new BufferedImage((int) Math.sqrt(normalizedData.length),
                    (int) Math.sqrt(normalizedData.length), BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = image.getRaster();

            for(int i = 0, index = 0; i < image.getHeight(); i++) {
                for(int j = 0; j < image.getHeight(); j++, index++) {
                    raster.setPixel(i, j, new int[] {normalizedData[index]});
                }
            }

            this.image.set(image);
            semaphore.release();
            System.out.println("Writing image...DONE");
        });
        thread.start();
    }

    @Contract(pure = true)
    private static byte normalize(int value) {
        double minInteger = Integer.MIN_VALUE;
        double maxInteger = Integer.MAX_VALUE;
        return (byte) ((value - minInteger) / (maxInteger - minInteger) * (Byte.MAX_VALUE - Byte.MIN_VALUE) + Byte.MIN_VALUE);
    }
}
