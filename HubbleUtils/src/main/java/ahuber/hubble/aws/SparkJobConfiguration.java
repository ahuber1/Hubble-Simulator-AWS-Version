package ahuber.hubble.aws;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A Java object corresponding to the JSON that is provided as input to for the Spark job that will sort the data
 * produced by the AWS Lambda function contained in {@link App}, and output the data in the
 */
public class SparkJobConfiguration {
    @SuppressWarnings({"unused"})
    @Contract(pure = true)
    public SparkJobConfiguration() {
        // Do nothing additional...
    }

    @Contract(pure = true)
    public SparkJobConfiguration(@NotNull  String name, int threshold, int...data) {
        this.satelliteName = Objects.requireNonNull(name, "The name cannot be null.");
        this.threshold = threshold;
        this.data = data;
    }

    /**
     * An {@code int} array containing the data to be sorted.
     */
    public int[] data;

    /**
     * The maximum number of elements in a sub-array that will need to be present in order for Insertion Sort to be
     * used instead of Merge Sort, i.e., subdividing the array into two halves.
     */
    @SuppressWarnings("WeakerAccess")
    public int threshold;

    /**
     * The name of the Satellite session that produced {@link #data} and specified the {@link #threshold}
     */
    @SuppressWarnings("WeakerAccess")
    public String satelliteName;
}
