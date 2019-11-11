package ahuber.hubble;

import ahuber.hubble.aws.App;
import ahuber.hubble.aws.SparkJobConfiguration;
import ahuber.hubble.sort.MergeSortInt;
import ahuber.hubble.utils.SatelliteImageWriter;
import ahuber.hubble.utils.Utils;
import org.javatuples.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

/**
 * Contains a driver program that executes what
 */
public class Driver {
    private static final boolean LAUNCH_EMR_CLUSTER = false;

    public static void main(String[] args) throws IOException {
        App app = new App();
        Pair<Long, Optional<SparkJobConfiguration>> pair = app.process(LAUNCH_EMR_CLUSTER,
                "ahuber-satellite-configs", "java/input_i10_j1.json");
        long elapsedMilliseconds = pair.getValue0();

        if (!LAUNCH_EMR_CLUSTER) {
            Optional<SparkJobConfiguration> optionalConfiguration = pair.getValue1();

            if (optionalConfiguration.isPresent()) {
                elapsedMilliseconds += process(optionalConfiguration.get());
            } else {
                throw new RuntimeException("Unable to process the data due to a non-existent SparkJobConfiguration.");
            }
        }

        System.out.printf("Simulation ran for %,d milliseconds\n", elapsedMilliseconds);

    }

    private static long process(SparkJobConfiguration configuration) throws IOException {
        long startMillis = System.currentTimeMillis();
        int[] data = configuration.getData();
        MergeSortInt.sort(data, configuration.getThreshold());
        BufferedImage image = SatelliteImageWriter.writeGreyscaleImage(data);
        File outputFile = new File(String.format("%s.jpg", configuration.getSatelliteName()));
        String outputAbsolutePath = outputFile.getAbsolutePath();

        if (ImageIO.write(image, "jpg",outputFile)) {
            System.out.printf("Image was successfully written to %s\n", outputAbsolutePath);
        } else {
            String message = String.format("Image was unable to be written to %s", outputAbsolutePath);
            throw new IOException(message);
        }

        if (Desktop.isDesktopSupported()) {
            System.out.printf("Opening file %s\n", outputAbsolutePath);
            Desktop desktop = Desktop.getDesktop();
            desktop.open(outputFile);
        } else {
            System.out.println("Desktop is not supported.");
        }

        return System.currentTimeMillis() - startMillis;
    }
}
