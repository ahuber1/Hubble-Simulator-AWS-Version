package ahuber.hubble;

import ahuber.hubble.adt.IntArrayWrapper;
import ahuber.hubble.adt.IntBuffer;
import ahuber.hubble.adt.SizeObserver;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

/**
 * An object that waits for enough data to be processed in an {@link IntBuffer} and sends the data to a
 * {@link Processor} when enough data is present.
 */
public class Receiver implements SizeObserver<IntBuffer>, Runnable {
    @NotNull private final IntBuffer buffer;
    @NotNull private final Processor<IntArrayWrapper> processor;
    @NotNull private Semaphore semaphore = new Semaphore(1);
    private final int threshold;

    /**
     * Creates a new {@link Receiver} object.
     * @param buffer The {@link IntBuffer} that this {@link Receiver} will observe.
     * @param processor The {@link Processor} that will receive the data when enough data is present.
     * @param threshold The amount of items that need to be present in the {@link IntBuffer} before it is sent off to
     *                 the {@link Processor}
     * @throws IllegalArgumentException If {@code threshold} is less than one.
     */
    public Receiver(@NotNull IntBuffer buffer, @NotNull Processor<IntArrayWrapper> processor, int threshold) {
        this.processor = processor;

        if (threshold < 1) {
            throw new IllegalArgumentException(String.format("The threshold cannot be null. Threshold: %d", threshold));
        }

        this.buffer = buffer;
        this.threshold = threshold;

        // Register this as an observer of the buffer
        this.buffer.registerObserver(this);

        // Acquire a semaphore before the run() method starts. This is done to ensure that the data is processed only
        // when enough has become available
        semaphore.acquireUninterruptibly();
    }

    @Override
    public void run() {
        try {
            // Immediately try to acquire a semaphore. The semaphore will be acquired when enough data has become
            // available in the IntBuffer
            semaphore.acquire();

            // Release the semaphore so we "clean up after ourselves."
            semaphore.release();

            // Take the first "threshold" values
            int[] values = buffer.take(threshold);

            // Place the values in an IntArrayWrapper before sending them to the Processor in a background thread
            IntArrayWrapper wrapper = new IntArrayWrapper(values);
            Thread processingThread = new Thread(() -> processor.onReceived(wrapper),"Processor");

            // Start the thread and wait for it to end
            processingThread.start();
            processingThread.join();
        } catch (InterruptedException ignored) {
            // Ignore
        } catch (Exception exception) {
            if (exception.getCause() instanceof InterruptedException) {
                return; // Ignore
            }

            throw exception;
        }
    }

    @Override
    public void sizeChanged(@NotNull IntBuffer collection) {
        if (collection.size() >= threshold) {
            // Release the semaphore that was acquired in the constructor, thereby letting the run()
            // method continue in its execution so it can alert the receiver.
            semaphore.release();
        }
    }
}
