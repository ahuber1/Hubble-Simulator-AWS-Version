package ahuber.hubble;

import ahuber.hubble.adt.Buffer;
import ahuber.hubble.adt.IntArrayWrapper;
import ahuber.hubble.adt.IntBuffer;
import ahuber.hubble.adt.SizeObserver;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

public class Receiver implements SizeObserver<IntBuffer>, Runnable {
    @NotNull private final IntBuffer buffer;
    @NotNull private final Processor<IntArrayWrapper> processor;
    @NotNull private Semaphore semaphore = new Semaphore(1);
    private final int threshold;

    public Receiver(@NotNull IntBuffer buffer, @NotNull Processor<IntArrayWrapper> processor, int threshold) {
        this.processor = processor;

        if (threshold < 1) {
            throw new IllegalArgumentException(String.format("The threshold cannot be null. Threshold: %d", threshold));
        }

        this.buffer = buffer;
        this.threshold = threshold;
        this.buffer.registerObserver(this);
        semaphore.acquireUninterruptibly();
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            semaphore.release();

            System.out.println("Receiving data...START");
            int[] values = buffer.take(threshold);
            IntArrayWrapper wrapper = new IntArrayWrapper(values);
            System.out.println("Receiving data...DONE");
            System.out.println("Processing data...START");
            Thread processingThread = new Thread(() -> processor.onReceived(wrapper),"Processor");
            processingThread.start();
            processingThread.join();
            System.out.println("Processing data...DONE");
        } catch (InterruptedException ignored) {
        } catch (Exception exception) {
            if (exception.getCause() instanceof InterruptedException) {
                return;
            }

            throw exception;
        }
    }

    @Override
    public void sizeChanged(@NotNull IntBuffer collection) {
        if (collection.size() >= threshold) {
            semaphore.release();
        }
    }
}
