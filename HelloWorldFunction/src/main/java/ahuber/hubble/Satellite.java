package ahuber.hubble;

import ahuber.hubble.adt.Buffer;
import ahuber.hubble.adt.IntBuffer;
import ahuber.hubble.adt.SizeObserver;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Satellite implements SizeObserver<IntBuffer>, Runnable {
    @NotNull private final IntBuffer buffer;
    @NotNull private final Semaphore semaphore = new Semaphore(1);
    @NotNull private final Random random = new Random();

    public Satellite(@NotNull IntBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void sizeChanged(IntBuffer collection) {
        semaphore.release();
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                int number = random.nextInt();
                boolean successful;

                do {
                    successful = buffer.add(number);

                    if (!successful) {
                        semaphore.acquire();
                        semaphore.acquire();
                        semaphore.release();
                    }
                } while(!successful);
            }

        } catch (InterruptedException ignored) {
        }
    }
}
