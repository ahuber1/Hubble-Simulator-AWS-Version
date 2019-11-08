package ahuber.hubble;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that processes data when it is available.
 * @param <T> The type of data that will be provided to the processor when it has become available.
 */
public interface Processor<T> {
    /**
     * A method that is invoked when the data has become available for processing.
     * @param data The data
     */
    void onReceived(@NotNull T data);
}
