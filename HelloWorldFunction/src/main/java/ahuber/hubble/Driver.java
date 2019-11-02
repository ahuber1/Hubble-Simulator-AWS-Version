package ahuber.hubble;

import ahuber.hubble.aws.App;
import ahuber.hubble.aws.SatelliteConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Driver {
    public static void main(String[] args) throws IOException {
        App app = new App();
        String output = app.process(new SatelliteConfiguration(11, 1));
        System.out.println(output);
    }
}
