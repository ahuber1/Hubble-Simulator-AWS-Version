package ahuber.hubble;

import ahuber.hubble.adt.IntBuffer;
import ahuber.hubble.adt.SizeObserver;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * A class representing the Hubble Space Telescope, which in this instance produces random integer data that is
 * placed inside of an {@link IntBuffer}.
 */
public class Satellite implements SizeObserver<IntBuffer>, Runnable {
    @NotNull private final IntBuffer buffer;
    @NotNull private final Semaphore semaphore = new Semaphore(1);
    @NotNull private final Random random = new Random();

    /**
     * Creates a new Hubble Space Telescope that puts random integer data in the provided {@link IntBuffer}
     * @param buffer The {@link IntBuffer} into which to place the random integer data.
     */
    public Satellite(@NotNull IntBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void sizeChanged(IntBuffer collection) {
        // Release the semaphore so we can alert the run() method that space may be available in the buffer to
        // add more items.
        semaphore.release();
    }

    /**
     * Starts the Hubble Space Telescope.
     */
    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                int number = random.nextInt();
                boolean successful;

                do {
                    successful = buffer.add(number);

                    if (successful) {
                        continue;
                    }

                    // If we were not successful, wait until the size of the collection changes and try again.
                    // We wait by acquiring a semaphore a first time and then a second time. The semaphore is released
                    // in sizeChanged(IntBuffer), which enables the semaphore to be acquired that second time.
                    semaphore.acquire();
                    semaphore.acquire();

                    // Now that we have the semaphore, release it so we "clean up after ourselves," and try again.
                    semaphore.release();
                } while(!successful);
            }

        } catch (InterruptedException ignored) {
            // Exit method because this thread was interrupted
        }
    }
}
