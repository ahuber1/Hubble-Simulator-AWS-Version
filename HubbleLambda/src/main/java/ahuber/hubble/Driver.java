package ahuber.hubble;

import ahuber.hubble.aws.App;
import ahuber.hubble.aws.SatelliteConfiguration;

import java.io.*;

/**
 * Contains a driver program that executes what
 */
public class Driver {
    public static void main(String[] args) throws IOException {
        App app = new App();
        long elapsedMilliseconds = app.process("ahuber-satellite-configs", "java/input_n8_j1.json");
        System.out.printf("Simulation ran for %,d milliseconds\n", elapsedMilliseconds);
    }
}
