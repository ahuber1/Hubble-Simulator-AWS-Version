package ahuber.hubble.spark;

import ahuber.hubble.aws.S3Helpers;
import ahuber.hubble.aws.SparkJobConfiguration;
import ahuber.hubble.sort.MergeSortInt;
import ahuber.hubble.utils.SatelliteImageWriter;
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

    private static final Regions DEFAULT_REGION = Regions.US_EAST_1;

    public static void main(String[] args) {
        try {
            // Download the SparkJobConfiguration from S3 at the link provided in args.
            SparkJobConfiguration jobConfiguration = extractSparkJobConfiguration(args);

            // Start a SparkSession and create a JavaSparkContext
            SparkSession session = SparkSession.builder()
                    .appName("Hubble_AWS_EMR")
                    .getOrCreate();
            JavaSparkContext context = new JavaSparkContext(session.sparkContext());

            // Sort the data using the Hadoop cluster.
            int[] sortedData = parallelMergeSort(context, jobConfiguration);
            session.stop();

            // Write the values to an image and upload it.
            BufferedImage image = SatelliteImageWriter.writeGreyscaleImage(sortedData);
            S3Helpers.uploadImage(image, Regions.US_EAST_1, "ahuber-satellite-images",
                    String.format("java/%s.jpg", jobConfiguration.getSatelliteName()));
        } catch (IOException e) {
            throw new RuntimeException("An I/O error occurred.", e);
        }
    }

    private static int[] parallelMergeSort(@NotNull JavaSparkContext context,
            @NotNull SparkJobConfiguration jobConfiguration) {
        int[] unsortedData = jobConfiguration.getData();
        int threshold = jobConfiguration.getThreshold();
        int end = unsortedData.length;
        int middle = end / 2;
        int[] leftHalf = Arrays.copyOfRange(unsortedData, 0, middle);
        int[] rightHalf = Arrays.copyOfRange(unsortedData, middle, end);
        JavaRDD<int[]> dataSet = context.parallelize(Arrays.asList(leftHalf, rightHalf), 2);
        System.out.printf("Threshold is %d, but array length is %d.\n", threshold, unsortedData.length);

        // Start map reduce
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
        Regions region = args.length >= 2 ? Regions.fromName(args[1]) : DEFAULT_REGION;
        S3Object download = S3Helpers.download(region, uri.getBucket(), uri.getKey());
        String json = S3Helpers.readAsString(download);
        SparkJobConfiguration jobConfiguration = ObjectMapperSingleton.getObjectMapper()
                .readValue(json, SparkJobConfiguration.class);
        download.close();
        return jobConfiguration;
    }

}
