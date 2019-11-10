package ahuber.hubble.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that can log text.
 */
public interface Logger {
    /**
     * Logs the provided string <i>without</i> a newline character.
     * @param str The string to log.
     */
    void log(@Nullable String str);

    /**
     * Logs the provided string and appends a newline character>
     * @param str The string to log.
     */
    default void logLine(String str) {
        log(Utils.requireNonNullElse(str, "") + "\n");
    }

    /**
     * Logs a string that is constructed using the provided format string and arguments <i>without</i> a newline
     * character appended to the end of the constructed string.
     * @param format A format string.
     * @param args The arguments for the format string.
     */
    default void log(String format, Object...args) {
        log(String.format(format, args));
    }

    /**
     * Logs a string that is constructed using the provided format string and arguments with a newline character
     * appended to the end of the constructed string.
     * @param format A format string.
     * @param args The arguments for the format string.
     */
    default void logLine(String format, Object...args) {
        logLine(String.format(format, args));
    }
}
