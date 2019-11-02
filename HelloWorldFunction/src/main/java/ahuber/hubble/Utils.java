package ahuber.hubble;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A class containing general purpose helper methods.
 */
public final class Utils {

    /**
     * Clamps the provided value in the specified range.
     * @param value The value to clamp.
     * @param inclusiveMin The inclusive lower bound of the range.
     * @param inclusiveMax The inclusive upper bound of the range.
     * @param <T> The type of value to clamp.
     * @return {@code value} clamped in the range [{@code inclusiveMin}, {@code inclusiveMax}].
     * @throws NullPointerException If {@code value}, {@code inclusiveMin}, or {@code inclusiveMax} are {@code null}.
     * @throws IllegalArgumentException If {@code inclusiveMax} &gt; {@code inclusiveMin}.
     */
    public static <T extends Comparable<T>> T clamp(@NotNull T value, @NotNull T inclusiveMin, @NotNull T inclusiveMax) {
        Objects.requireNonNull(value, "The value cannot be null");
        Objects.requireNonNull(inclusiveMin, "The inclusive min cannot be null.");
        Objects.requireNonNull(inclusiveMax, "The inclusive max cannot be null.");
        int minMaxCompareResult = inclusiveMin.compareTo(inclusiveMax);

        if (minMaxCompareResult > 0) {
            String message = String.format("Invalid combination of inclusive min and max. Inclusive Min: %s, " +
                    "Inclusive Max: %s", inclusiveMin, inclusiveMax);
            throw new IllegalArgumentException(message);
        }

        if (value.compareTo(inclusiveMin) < 0) {
            return inclusiveMin;
        }

        if (value.compareTo(inclusiveMax) > 0) {
            return inclusiveMax;
        }

        return value;
    }

    /**
     * Executes the provided {@link Runnable} and returns a {@code long} containing the time it took to execute the
     * {@link Runnable} in milliseconds.
     * @param runnable The {@link Runnable}
     * @return A {@code long} containing the time it took to execute the {@link Runnable} in milliseconds.
     * @exception NullPointerException The {@link Runnable} was null.
     */
    public static long timeMillis(Runnable runnable) {
        return timeMillis(() -> {
            Objects.requireNonNull(runnable, "The runnable cannot be null");
            return Void.TYPE;
        }).getValue1();
    }

    /**
     * Executes the provided {@link Supplier} and returns a {@link Pair} containing the returned value and how long
     * it took to execute the {@link Supplier} in milliseconds
     * @param supplier The {@link Supplier}
     * @param <T> An arbitrary data type corresponding to the type of data returned by the {@link Supplier}
     * @return A {@link Pair} containing the data returned by the {@link Supplier} and the time it took to execute
     * the {@link Supplier} in milliseconds.
     * @exception NullPointerException The {@link Supplier} was null.
     */
    @NotNull
    @Contract("null -> fail")
    public static <T> Pair<T, Long> timeMillis(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        T value = Objects.requireNonNull(supplier, "supplier cannot be null").get();
        long end = System.currentTimeMillis();
        long elapsedMilliseconds = end - start;
        return new Pair<>(value, elapsedMilliseconds);
    }

    /**
     * Returns an array containing the specified items.
     * @param items The items in the array.
     * @param <T> The type of items in the array.
     * @return The specified items in the order they are specified in an array.
     */
    @SafeVarargs
    @Contract(value = "_ -> param1", pure = true)
    public static <T> T[] arrayOf(T...items) {
        return items;
    }

    /**
     * Checks a value to determine if it is {@code null} and, if it is, returns a non-null default value.
     * @param value The value to check.
     * @param defaultValue The non-null default value to return if {@code value} is {@code null}
     * @param <T> The type of value to check.
     * @return {@code value} if {@code value} is not {@code null}, or {@code defaultValue} is {@code value} is {@code
     * null} and {@code defaultValue} is not {@code null}.
     * @throws NullPointerException If {@code value} and {@code defaultValue} are {@code null}.
     */
    @Contract("!null, _ -> param1; null, !null -> param2; null, null -> fail")
    public static <T> T requireNonNullElse(T value, T defaultValue) {
        return requireNonNullElse(value, defaultValue, x -> x);
    }

    /**
     * Checks a value to determine if it is {@code null} and, if it is not {@code null}, invokes a transformation
     * function that transforms that non-null value to the return type before returning it. Otherwise, if the
     * value is {@code null}, the provided default value is returned.
     * @param value The value to check.
     * @param defaultValue The value that will be returned if {@code value} is {@code null}
     * @param transformation A function that transforms {@code value} to the return type when {@code value} is not
     * {@code null}
     * @param <T> The input type.
     * @param <R> The return type.
     * @return The value returned by {@code transformation} when {@code value} is not {@code null} and is the input
     * for {@code transformation}, or {@code defaultValue} if {@code value} is {@code null} and {@code defaultValue}
     * is not.
     * @throws NullPointerException If {@code value} is not {@code null} and {@code transformation} is {@code null}, or
     * if both {@code value} and {@code defaultValue} are null.
     */
    @Contract("!null, _, !null -> _; !null, _, null -> fail; null, !null, _ -> param2; null, null, _ -> fail")
    public static <T, R> R requireNonNullElse(T value, R defaultValue, Function<T, R> transformation) {
        if (Objects.nonNull(value)) {
            return Objects.requireNonNull(transformation, "'transformation' cannot be null").apply(value);
        }

        if (Objects.nonNull(defaultValue)) {
            return defaultValue;
        }

        throw new NullPointerException("Both the provided value and default value are null.");
    }

    /**
     * Reads all characters from the {@link Reader}, and returns those characters as a string.
     * @param reader The {@link Reader}
     * @return The string containing all the characters in the {@link Reader}
     * @throws IOException If an I/O error occurs.
     */
    @SuppressWarnings("unused")
    @Contract("null -> fail")
    @NotNull
    public static String readAllAsString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        Objects.requireNonNull(reader, "'reader' cannot be null.");
        int charCode;

        while ((charCode = reader.read()) != -1) {
            builder.append(charCode);
        }

        return builder.toString();
    }

    /**
     * Returns a {@link Logger} that can be used during the execution of the program.
     * @param context The {@link Context} provided by AWS Lambda.
     * @return The {@link Logger}
     * @implNote  If there is a {@link Context} with a {@linkplain Context#getLogger()} logger}, then that
     * {@link LambdaLogger} is used in the implementation of the returned {@link Logger}. Otherwise,
     * {@link System#out} is used.
     */
    @NotNull
    public static Logger getLogger(@Nullable Context context) {
        LambdaLogger lambdaLogger;

        if (context == null || (lambdaLogger = context.getLogger()) == null) {
            return System.out::print;
        }

        return lambdaLogger::log;
    }

    @Contract("null -> !null")
    public static Stream<Boolean> streamOf(boolean...values) {
        if (values == null) {
            return Stream.empty();
        }

        return IntStream.range(0, values.length).mapToObj(index -> values[index]);
    }
}
