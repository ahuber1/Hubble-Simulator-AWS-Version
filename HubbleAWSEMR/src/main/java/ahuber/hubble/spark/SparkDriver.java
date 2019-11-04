package ahuber.hubble.spark;

import ahuber.hubble.SatelliteImageWriter;
import ahuber.hubble.aws.S3Helpers;
import ahuber.hubble.aws.SparkJobConfiguration;
import ahuber.hubble.sort.MergeSortInt;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SparkDriver {
    public static void main(String[] args) {
        try {
            SparkSession session = SparkSession.builder()
                    .appName("Hubble_AWS_EMR")
                    .getOrCreate();

            JavaSparkContext context = new JavaSparkContext(session.sparkContext());
            SparkJobConfiguration jobConfiguration = extractSparkJobConfiguration(args);
            int[] sortedData = parallelMergeSort(context, jobConfiguration);
            session.stop();
            BufferedImage image = SatelliteImageWriter.writeGreyscaleImage(sortedData);
            S3Helpers.uploadImage(image, Regions.US_EAST_1, "ahuber-satellite-images",
                    String.format("java/%s.jpg", jobConfiguration.satelliteName));
        } catch (IOException e) {
            throw new RuntimeException("An I/O error occurred.", e);
        }
    }

    private static int[] parallelMergeSort(@NotNull JavaSparkContext context, @NotNull SparkJobConfiguration jobConfiguration) {
        int[] unsortedData = jobConfiguration.data;
        int threshold = jobConfiguration.threshold;
        int end = unsortedData.length;
        int middle = end / 2;
        int[] leftHalf = Arrays.copyOfRange(unsortedData, 0, middle);
        int[] rightHalf = Arrays.copyOfRange(unsortedData, middle, end);
        JavaRDD<int[]> dataSet = context.parallelize(Arrays.asList(leftHalf, rightHalf), 2);
        System.out.printf("Threshold is %d, but array length is %d.\n", threshold, unsortedData.length);
        return dataSet.map(array -> {
            MergeSortInt.sort(array, threshold);
            return array;
        }).reduce(MergeSortInt::merge);
    }

    @NotNull
    private static SparkJobConfiguration extractSparkJobConfiguration(String[] args) throws IOException {
        if (Objects.requireNonNull(args, "'args' cannot be null.").length < 1) {
            throw new RuntimeException("s3 URI for JSON configuration not specified.");
        }

        AmazonS3URI uri = new AmazonS3URI(args[0]);
        Regions region = args.length >= 2 ? Regions.fromName(args[1]) : Regions.US_EAST_1;
        return getSparkJobConfiguration(uri, region);
    }

    @NotNull
    private static SparkJobConfiguration getSparkJobConfiguration(@NotNull AmazonS3URI uri, Regions region) throws IOException {
        S3Object download = S3Helpers.download(region, uri.getBucket(), uri.getKey());
        String json = S3Helpers.readAsString(download);
        SparkJobConfiguration jobConfiguration = ObjectMapperSingleton.getObjectMapper()
                .readValue(json, SparkJobConfiguration.class);
        download.close();
        return jobConfiguration;
    }
}
