package ahuber.hubble;

import ahuber.hubble.aws.App;
import ahuber.hubble.aws.SatelliteConfiguration;

import java.io.*;

public class Driver {
    public static void main(String[] args) throws IOException {
        App app = new App();
        String output = app.process("ahuber-satellite-configs", "java/input_n8_j1.json");
        System.out.println(output);
    }
}
