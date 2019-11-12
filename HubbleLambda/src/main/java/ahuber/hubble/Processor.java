package ahuber.hubble;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an object that processes data when it is available.
 * @param <T_INPUT> The type of data that will be provided to the processor when it has become available.
 */
public interface Processor<T_INPUT, T_RESULT> {
    /**
     * A method that is invoked when the data has become available for processing.
     * @param data The data
     */
    void onReceived(@NotNull T_INPUT data);

    /**
     * Gets the result from this {@link Processor}
     * @return The result from this {@link Processor} or {@link Optional#empty()} if the result is not available yet.
     */
    @NotNull
    Optional<T_RESULT> getResult();
}
