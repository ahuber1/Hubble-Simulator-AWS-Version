package ahuber.hubble.lambda;

public class SatelliteConfiguration {
    public int i;

    @SuppressWarnings("WeakerAccess")
    public int j;

    @Override
    public String toString() {
        return String.format("i = {%d}, j = {%d}", i, j);
    }
}
