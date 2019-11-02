package ahuber.hubble;

import org.intellij.lang.annotations.PrintFormat;

public interface Logger {
    void log(String str);

    default void logLine(String str) {
        log(Utils.requireNonNullElse(str, "") + "\n");
    }

    default void log(String format, Object...args) {
        log(String.format(format, args));
    }

    default void logLine(String format, Object...args) {
        logLine(String.format(format, args));
    }
}
