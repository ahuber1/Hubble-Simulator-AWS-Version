package ahuber.hubble.aws;

import ahuber.hubble.Receiver;
import ahuber.hubble.Satellite;
import ahuber.hubble.SatelliteProcessor;
import ahuber.hubble.Utils;
import ahuber.hubble.adt.IntBuffer;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3URI;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;

public class App implements RequestStreamHandler {
    private Regions emrRegion = Regions.US_EAST_1;
    private final String satelliteName;
    private final AmazonS3URI logUri;
    private final AmazonS3URI sparkJobConfigUri;
    private final AmazonS3URI sparkJobJarUri;
    private final String[] sparkJobJarArgs;

    public App() {
        satelliteName = generateName();
        logUri = new AmazonS3URI(S3Helpers.createHadoopLogFolderUri(satelliteName));
        sparkJobConfigUri = new AmazonS3URI(S3Helpers.createSparkJobConfigUri(satelliteName));
        sparkJobJarUri = new AmazonS3URI(S3Helpers.createSparkJobJarUri("TODO")); // TODO
        sparkJobJarArgs = new String[] { "TODO" }; // TODO
    }


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        // Convert the input stream to a byte array
        byte[] bytes = IOUtils.toByteArray(input);

        // Convert the byte array to a string in UTF-8 encoding. The string contains JSON
        String json = new String(bytes, StandardCharsets.UTF_8);

        // Convert JSON to Java object
        SatelliteConfiguration configuration = ObjectMapperSingleton.getObjectMapper().readValue(json,
                SatelliteConfiguration.class);

        // Calculate variables that determine how the satellite will behave
        int n = (int) Math.pow(2, configuration.i);
        int t = (int) Math.pow(10, configuration.j);
        int receiverThreshold = (int) Math.pow(n, 2);
        int bufferSize = receiverThreshold * 2;
        Consumer<String> logFunction;

        if (context == null) {
            logFunction = System.out::println;
        } else {
            logFunction = string -> {
                LambdaLogger logger = context.getLogger();
                logger.log(string);
            };
        }

        logFunction.accept(String.format("Running simulation: \"{%s}\"\n\tn = {%d}, t = {%d}, bufferSize = {%d}, " +
                "receiverThreshold = {%d}", satelliteName, n, t, bufferSize, receiverThreshold));

        // Create the buffer, satellite, processor, and receiver
        IntBuffer buffer = new IntBuffer(bufferSize);
        Satellite satellite = new Satellite(buffer);
        SatelliteProcessor processor = new SatelliteProcessor(satelliteName, receiverThreshold, emrRegion, logUri,
                sparkJobConfigUri, sparkJobJarUri, sparkJobJarArgs);
        Receiver receiver = new Receiver(buffer, processor, receiverThreshold);

        // Create the threads
        Thread satelliteThread = new Thread(satellite, "Satellite");
        Thread processorThread = new Thread(processor, "Processor");
        Thread receiverThread = new Thread(receiver, "Receiver");

        // Run the threads inside a timed block.
        long elapsedMilliseconds = Utils.timeMillis(() -> {
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

        // Log the elapsed duration
        logFunction.accept(String.format("Satellite has been shut down. Satellite ran for %,d milliseconds.",
                elapsedMilliseconds));
    }

    private static String generateName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        UUID id = UUID.randomUUID();
        String dateString = formatter.format(now);
        return String.format("%s_%s", dateString, id);
    }
}