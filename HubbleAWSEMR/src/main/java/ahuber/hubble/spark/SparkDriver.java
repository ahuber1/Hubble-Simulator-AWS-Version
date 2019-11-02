package ahuber.hubble.spark;

import ahuber.hubble.SatelliteImageWriter;
import ahuber.hubble.Utils;
import ahuber.hubble.aws.S3Helpers;
import ahuber.hubble.aws.LocalizedS3ObjectId;
import ahuber.hubble.aws.SparkJobConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.amazonaws.jmespath.ObjectMapperSingleton.getObjectMapper;

public class SparkDriver {
    public static void main(String[] args) throws IOException {
        Objects.requireNonNull(args, "'args' cannot be null.");

        if (args.length < 1) {
            throw new RuntimeException("s3 URI for JSON configuration not specified.");
        }

        AmazonS3URI uri = new AmazonS3URI(args[0]);
        Regions region = args.length >= 2 ? Regions.fromName(args[1]) : Regions.US_EAST_1;
        SparkJobConfiguration jobConfiguration = getSparkJobConfiguration(uri, region);
        int[] data = SparkMergeSort.sort(jobConfiguration.data, jobConfiguration.threshold);
        BufferedImage bufferedImage = SatelliteImageWriter.writeGreyscaleImage(data);
        LocalizedS3ObjectId imageId = S3Helpers.createImageId(jobConfiguration.satelliteName);
        S3Helpers.uploadImage(bufferedImage, imageId);
        System.out.printf("Image uploaded to %s\n", imageId);
    }

    @NotNull
    private static SparkJobConfiguration getSparkJobConfiguration(@NotNull AmazonS3URI uri, Regions region) throws IOException {
        S3Object download = S3Helpers.download(region, uri.getBucket(), uri.getKey());
        S3ObjectInputStream objectContent = download.getObjectContent();
        InputStreamReader inputStreamReader = new InputStreamReader(objectContent);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String json = Utils.readAllAsString(bufferedReader);
        SparkJobConfiguration jobConfiguration = getObjectMapper()
                .readValue(json, SparkJobConfiguration.class);
        bufferedReader.close();
        inputStreamReader.close();
        download.close();
        return jobConfiguration;
    }
}
