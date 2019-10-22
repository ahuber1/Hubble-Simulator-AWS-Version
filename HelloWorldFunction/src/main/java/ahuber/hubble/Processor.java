package ahuber.hubble;

import org.jetbrains.annotations.NotNull;

public interface Processor<T> {
    void onReceived(@NotNull T data);
}
