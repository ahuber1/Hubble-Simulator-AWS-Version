package ahuber.hubble.lambda;

import ahuber.hubble.Receiver;
import ahuber.hubble.Satellite;
import ahuber.hubble.SatelliteProcessor;
import ahuber.hubble.adt.IntArrayWrapper;
import ahuber.hubble.adt.IntBuffer;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class App implements RequestStreamHandler {

    @NotNull private final String name;

    public App() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        UUID id = UUID.randomUUID();
        this.name = String.format("%s_%s", dtf.format(now), id);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, @Nullable Context context) throws IOException {
        byte[] inputBytes = IOUtils.toByteArray(input);
        String json = new String(inputBytes, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        SatelliteConfiguration configuration = objectMapper.readValue(json, SatelliteConfiguration.class);
        int n = (int) Math.pow(2, configuration.i);
        int t = (int) Math.pow(10, configuration.j);
        int bufferSize = (n * n * 2);
        int receiverThreshold = n * n;

        String secondLine = String.format("n = {%d}, t = {%d}, bufferSize = {%d}, receiverThreshold = {%d}", n, t,
                bufferSize, receiverThreshold);

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log(configuration.toString());
            logger.log(secondLine);
        } else {
            System.out.println(configuration);
            System.out.println(secondLine);
        }

        long startMillis = System.currentTimeMillis();
        IntBuffer buffer = new IntBuffer(bufferSize);
        Satellite satellite = new Satellite(buffer);
        SatelliteProcessor processor = new SatelliteProcessor(t);
        Receiver receiver = new Receiver(buffer, processor, receiverThreshold);
        Thread satelliteThread = new Thread(satellite, "Satellite");
        Thread processorThread = new Thread(processor, "Processor");
        Thread receiverThread = new Thread(receiver, "Receiver");
        processorThread.start();
        receiverThread.start();
        satelliteThread.start();

        try {
            processorThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        satelliteThread.interrupt();
        satelliteThread.interrupt();
        long endMillis = System.currentTimeMillis();
        long duration = endMillis - startMillis;
        System.out.printf("Satellite ran for %,d ms\n", duration);
        BufferedImage image = processor.getImage();

        if (image == null) {
            throw new RuntimeException("No image");
        }

        ImageIO.write(image, "jpg", output);
        uploadToS3(image);
    }

    private void uploadToS3(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "danshi";

        try {
            //This code expects that you have AWS credentials set up per:
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            // Upload a file as a new object with ContentType and title specified.

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            metadata.addUserMetadata("x-amz-meta-title", getName());
            PutObjectRequest request = new PutObjectRequest(bucketName, getName(), inputStream, metadata);

            s3Client.putObject(request);
        } catch (SdkClientException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            //
            // OR
            //
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

    }
}