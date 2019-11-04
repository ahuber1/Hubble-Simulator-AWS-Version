package ahuber.hubble.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.junit.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestingUtilities {
    public static String getStackTraceString(@NotNull Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static <T extends Throwable> void assertExceptionThrown(@NotNull Runnable runnable, @NotNull Class<T> type) {
        assertExceptionThrown(runnable, type, null);
    }

    @TestOnly
    public static <T extends Throwable> void assertExceptionThrown(@NotNull Runnable runnable, @NotNull Class<T> type,
                                                                   @Nullable Validator<T> validator) {
        try
        {
            runnable.run();
        } catch (Throwable throwable) {
            String message;

            if (type.isInstance(throwable)) {
                boolean isValid = validator == null || validator.isValid(type.cast(throwable));

                if (isValid) {
                    return;
                }

                message = String.format("Although an exception of type \"%s\" was thrown, it was not valid.", type.getName());
            } else {
                message = String.format("Expected an exception of type \"%s\" to be thrown, but an exception of type " +
                        "\"%s\" was thrown.", type.getName(), throwable.getClass().getName());
            }

            String stackTrace = getStackTraceString(throwable);
            String fullMessage = String.join("\n", message, stackTrace);
            Assert.fail(fullMessage);
        }

        Assert.fail(String.format("An exception of type \"%s\" was not thrown.", type.getName()));
    }
}