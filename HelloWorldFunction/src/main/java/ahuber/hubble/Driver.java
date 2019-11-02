package ahuber.hubble;

import ahuber.hubble.aws.App;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Driver {
    public static void main(String[] args) throws IOException {
        App app = new App();
        String json = "{ \"i\": 11, \"j\": 1}";
        InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        app.handleRequest(input, output, null);

        File file = new File(app.getSatelliteName() + ".jpg");
        System.out.println(file.getAbsolutePath());
        byte[] data = output.toByteArray();
        ByteArrayInputStream input2 = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(input2);
        ImageIO.write(image, "jpg", output);
        output.flush();
    }
}
