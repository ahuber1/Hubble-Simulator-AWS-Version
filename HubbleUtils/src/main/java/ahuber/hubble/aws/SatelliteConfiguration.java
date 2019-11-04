package ahuber.hubble.aws;

import org.jetbrains.annotations.Contract;

/**
 * A Java object corresponding to the JSON that is provided as input to the AWS Lambda function in {@link App}
 */
public class SatelliteConfiguration {
    /**
     * The <i>i</i> value
     */
    public int i;

    /**
     * The <i>j</i> value.
     */
    @SuppressWarnings("WeakerAccess")
    public int j;

    @Contract(pure = true)
    public SatelliteConfiguration() {
        // Do nothing additional...
    }

    @Contract(pure = true)
    public SatelliteConfiguration(int i, int j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public String toString() {
        return String.format("i = {%d}, j = {%d}", i, j);
    }
}
