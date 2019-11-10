package ahuber.hubble.aws;

import ahuber.hubble.Receiver;
import ahuber.hubble.Satellite;
import ahuber.hubble.SatelliteProcessor;
import ahuber.hubble.adt.IntBuffer;
import ahuber.hubble.utils.Logger;
import ahuber.hubble.utils.Utils;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * The AWS Lambda function that responds to an S3 event for a JSON file being uploaded to an S3 bucket that can be
 * deserialized as a {@link SatelliteConfiguration} that specifies the <i>i</i> and <i>j</i> values that determines
 * how the Hubble Satellite simulation runs.
 */
public class App implements RequestHandler<S3Event, String> {
    private static final Regions EMR_REGION = Regions.US_EAST_1;
    private static final String SPARK_JOB_CLASS = "ahuber.hubble.spark.SparkDriver";

    @Override
    public String handleRequest(S3Event input, Context context) {
        Logger logger = Utils.getLogger(context);

        // Null check on input
        if (input == null) {
            return "No input was received";
        }

        // Get the records
        List<S3EventNotification.S3EventNotificationRecord> records = input.getRecords();

        if (records == null) {
            return "Records are null";
        }

        if (records.isEmpty()) {
            return "No records present";
        }

        logger.logLine("%s contains %d events", S3Event.class.getName(), records.size());

        // Process each S3Event
        String[] resultMapping = new String[records.size()];
        boolean[] errorMapping = new boolean[records.size()];
        Arrays.fill(errorMapping, true);
        processRecords(records, resultMapping, errorMapping, logger);

        // Construct output string
        String output = getOutput(resultMapping, errorMapping);
        logger.logLine(output);
        boolean errorOccurred = Utils.streamOf(errorMapping).anyMatch(wasError -> wasError);

        if (errorOccurred) {
            System.exit(-1);
        }

        return output;
    }

    /**
     * Provides an alternative entry point for the AWS Lambda function where instead of the input being an
     * {@link S3Event}, the input is the S3 bucket and key for a pre-existing JSON file that can be deserialized as a
     * {@link SatelliteConfiguration} object that determines how this Hubble Satellite simulation will run.
     *
     * @param bucket The bucket name.
     * @param key    The key for the S3 object that corresponds to the JSON file that can be deserialized as a
     *               {@link SatelliteConfiguration} object that determines how this Hubble Satellite simulation will
     *               run.
     * @return The number of milliseconds the satellite was running.
     * @throws IOException If an I/O error occurs.
     */
    public long process(@NotNull String bucket, @NotNull String key) throws IOException {
        return processS3Entity(bucket, key, Utils.getLogger(null));
    }

    @NotNull
    private String getOutput(@NotNull String[] resultMapping, boolean[] errorMapping) {
        StringBuilder builder = new StringBuilder("PROCESSED ALL S3 EVENTS\n\n");

        for (int i = 0; i < resultMapping.length; i++) {
            String result = resultMapping[i];
            boolean error = errorMapping[i];

            builder.append(String.format("[%d]: %s\n", i, error ? "ERROR" : "SUCCESS"));
            builder.append(result);

            if (i + 1 >= resultMapping.length) {
                continue;
            }

            builder.append('\n');
            builder.append('\n');
        }

        return builder.toString();
    }

    private void processRecords(@NotNull List<S3EventNotification.S3EventNotificationRecord> records,
            String[] resultMapping, boolean[] errorMapping, Logger logger) {

        for (int i = 0; i < records.size(); i++) {
            logger.logLine("Processing %s %d of %d",
                    S3EventNotification.S3EventNotificationRecord.class.getSimpleName(), i + 1, records.size());

            // Get the S3EventNotificationRecord at index i
            S3EventNotification.S3EventNotificationRecord record = records.get(i);

            if (record == null) {
                resultMapping[i] = createUnableToGetMessage(S3EventNotification.S3EventNotificationRecord.class);
                continue;
            }

            // Get the S3Entity
            S3EventNotification.S3Entity s3Entity = record.getS3();

            if (s3Entity == null) {
                resultMapping[i] = createUnableToGetMessage(S3EventNotification.S3Entity.class);
                continue;
            }

            // Get the key
            String key = extractKey(s3Entity, i, resultMapping);

            if (key == null) {
                continue;
            }

            // Get the bucket
            String bucket = extractBucket(s3Entity, i, resultMapping);

            if (bucket == null) {
                continue;
            }

            try {
                long elapsedMilliseconds = processS3Entity(bucket, key, logger);
                resultMapping[i] = String.format("Satellite has been shut down. " +
                        "Satellite ran for %,d milliseconds", elapsedMilliseconds);
                errorMapping[i] = false; // Indicate that this S3 Object was successfully processed.
            } catch (Exception e) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                try {
                    PrintStream printStream = new PrintStream(outputStream, true, "UTF-8");
                    e.printStackTrace(printStream);
                    resultMapping[i] = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                } catch (UnsupportedEncodingException ex) {
                    // Should never happen, but is here just in case.
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Nullable
    private String extractBucket(@NotNull S3EventNotification.S3Entity s3Entity, int i, String[] resultMapping) {
        S3EventNotification.S3BucketEntity bucketEntity = s3Entity.getBucket();

        if (bucketEntity == null) {
            resultMapping[i] = createUnableToGetMessage(S3EventNotification.S3BucketEntity.class);
            return null;
        }

        String bucket = bucketEntity.getName();

        if (bucket == null) {
            resultMapping[i] = "Bucket was null. Skipping...";
            return null;
        }
        return bucket;
    }

    @Nullable
    private String extractKey(@NotNull S3EventNotification.S3Entity s3Entity, int i, String[] resultMapping) {
        S3EventNotification.S3ObjectEntity objectEntity = s3Entity.getObject();

        if (objectEntity == null) {
            resultMapping[i] = createUnableToGetMessage(S3EventNotification.S3ObjectEntity.class);
            return null;
        }

        String key = objectEntity.getKey();

        // Check for null key
        if (key == null) {
            resultMapping[i] = "Key was null. Skipping...";
            return null;
        }

        // Check if key corresponds to JSON file
        if (!key.endsWith(".json")) {
            resultMapping[i] = String.format("S3 Object with key '%s' does not end in '.json'. Skipping...", key);
            return null;
        }
        return key;
    }

    private static String createUnableToGetMessage(@NotNull Class<?> classObject) {
        return String.format("Unable to get %s", classObject.getSimpleName());
    }

    private long processS3Entity(@NotNull String bucket, @NotNull String key, @NotNull Logger logger) throws IOException {
        // Download the content
        logger.logLine("Downloading S3 object located in bucket \"%s\" and that has key \"%s\"...", bucket, key);

        S3Object object = S3Helpers.download(bucket, key);
        String content = S3Helpers.readAsString(object);
        object.close();

        logger.logLine("JSON has been read: %s", content);

        // Convert JSON to Java object
        SatelliteConfiguration configuration = ObjectMapperSingleton.getObjectMapper()
                .readValue(content, SatelliteConfiguration.class);
        return process(configuration, logger);

    }

    private long process(@NotNull SatelliteConfiguration configuration, @NotNull Logger logger) {
        S3SatelliteSessionConfig sessionConfig =
                new S3SatelliteSessionConfig(configuration.getI(), configuration.getJ());

        // Calculate variables that determine how the satellite will behave
        int n = (int) Math.pow(2, configuration.getI());
        int t = (int) Math.pow(10, configuration.getJ());
        int receiverThreshold = (int) Math.pow(n, 2);
        int bufferSize = receiverThreshold * 2;

        logger.logLine("Running simulation: \"{%s}\"\n\tn = {%d}, t = {%d}, bufferSize = {%d}, " +
                "receiverThreshold = {%d}", sessionConfig.getSatelliteName(), n, t, bufferSize, receiverThreshold);

        // Create the buffer, satellite, processor, and receiver
        IntBuffer buffer = new IntBuffer(bufferSize);
        Satellite satellite = new Satellite(buffer);
        SatelliteProcessor processor = new SatelliteProcessor(
                array -> new SparkJobConfiguration(sessionConfig.getSatelliteName(), t, array), EMR_REGION,
                sessionConfig.getLogFolderId(), sessionConfig.getSparkJobConfigId(), sessionConfig.getSparkJobJarId(),
                SPARK_JOB_CLASS, sessionConfig.getSparkJobJarArgs());
        Receiver receiver = new Receiver(buffer, processor, receiverThreshold);

        // Create the threads
        Thread satelliteThread = new Thread(satellite, "Satellite");
        Thread processorThread = new Thread(processor, "Processor");
        Thread receiverThread = new Thread(receiver, "Receiver");

        // Run the threads inside a timed block.
        return Utils.timeMillis(() -> {
            // Start the threads.
            satelliteThread.start();
            processorThread.start();
            receiverThread.start();

            // Wait for the processor thread to join
            try {
                processorThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to join the Satellite thread", e);
            }

            // Interrupt the two remaining threads
            satelliteThread.interrupt();
            receiverThread.interrupt();
        });
    }

    @Value
    @NotNull
    private static class S3SatelliteSessionConfig {
        private final String satelliteName;
        private final LocalizedS3ObjectId logFolderId;
        private final LocalizedS3ObjectId sparkJobConfigId;
        private final LocalizedS3ObjectId sparkJobJarId;
        private final String[] sparkJobJarArgs;

        S3SatelliteSessionConfig(int i, int j) {
            LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String dateString = formatter.format(now);

            satelliteName = String.format("%s_i=%d_j=%d", dateString, i, j);
            logFolderId = S3Helpers.createHadoopLogFolderId(satelliteName);
            sparkJobConfigId = S3Helpers.createSparkJobConfigId(satelliteName);
            sparkJobJarId = S3Helpers.createSparkJobJarId("java/HubbleAWSEMR-1.0.jar");
            sparkJobJarArgs = new String[]{sparkJobConfigId.getStringUri(), Regions.US_EAST_1.getName()};
        }
    }
}