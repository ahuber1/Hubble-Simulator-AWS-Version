package ahuber.hubble.aws;

import ahuber.hubble.utils.WarningSuppressionReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.jetbrains.annotations.Contract;

/**
 * A Java object corresponding to the JSON that is provided as input to the AWS Lambda function.
 */
@Value
@SuppressWarnings("unused")
@WarningSuppressionReason("Used in external project.")
public class SatelliteConfiguration {
    /**
     * Gets the <i>i</i> value
     */
    private final int i;

    /**
     * Gets the <i>j</i> value.
     */
    private final int j;

    /**
     * Creates a new {@link SatelliteConfiguration}
     * @param i The <i>i</i> value.
     * @param j The <i>j</i> value.
     */
    @Contract(pure = true)
    public SatelliteConfiguration(@JsonProperty("i") int i, @JsonProperty("j") int j) {
        this.i = i;
        this.j = j;
    }
}