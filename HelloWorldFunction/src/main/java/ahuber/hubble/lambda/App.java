package ahuber.hubble.lambda;

import ahuber.hubble.Receiver;
import ahuber.hubble.Satellite;
import ahuber.hubble.SatelliteImageWriter;
import ahuber.hubble.SatelliteProcessor;
import ahuber.hubble.adt.IntBuffer;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        SatelliteImageWriter.uploadToS3(image, Regions.US_EAST_1, "danshi", getName());
    }
}