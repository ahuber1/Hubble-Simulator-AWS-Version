package ahuber.hubble.aws;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A Java object corresponding to the JSON that is provided as input to for the Spark job that will sort the data
 * produced by the AWS Lambda function contained in {@link App}, and output the data in the
 */
@Value
public class SparkJobConfiguration {

    /**
     * Gets the name of the satellite session that produced {@linkplain #getData()} the data} and specified
     * {@linkplain #getThreshold() the threshold}.
     */
    @NotNull private String satelliteName;

    /**
     * Gets ma the maximum number of elements in a sub-array that will need to be present in order for Insertion Sort
     * to be used instead of Merge Sort, i.e., subdividing the array into two halves.
     */
    private int threshold;

    /**
     * An {@code int} array containing the data to be sorted.
     */
    private int[] data;

    public SparkJobConfiguration(@JsonProperty("satelliteName") @NotNull String satelliteName,
            @JsonProperty("threshold") int threshold, @JsonProperty("data") int...data) {

        this.satelliteName = Objects.requireNonNull(satelliteName, "'satelliteName' cannot be null.");
        this.threshold = threshold;
        this.data = Objects.requireNonNull(data, "'data' cannot be null.");
    }
}
