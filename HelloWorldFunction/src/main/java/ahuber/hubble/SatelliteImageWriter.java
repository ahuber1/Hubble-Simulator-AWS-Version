package ahuber.hubble;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * A class containing utility functions for normalizing {@code int} data and writing it as an image.
 */
@SuppressWarnings("WeakerAccess")
public final class SatelliteImageWriter {

    //region Write Greyscale Image

    /**
     * Normalizes the provided {@code int} array into a {@code byte} array, and writes the resulting bytes into a
     * square greyscale image. This is an O(n) operation.
     * @param data The {@code byte} array.
     * @return A {@link BufferedImage} representing the square greyscale image generated using the data in the input
     * {@code int} array.
     * @throws NullPointerException If the {@code int} array was {@code null}
     * @see #normalize(int[])
     * @see #writeGreyscaleImage(byte[])
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static BufferedImage writeGreyscaleImage(int[] data) {
        return writeGreyscaleImage(normalize(data));
    }

    /**
     * Writes the data contained in the {@code byte} array to a square greyscale image. This is an O(n) operation.
     * @param data The {@code byte} array.
     * @return A {@link BufferedImage} representing the square greyscale image generated using the data in the input
     * {@code byte} array.
     * @throws NullPointerException If the {@code byte} array was {@code null}
     * @see #writeGreyscaleImage(int[])
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static BufferedImage writeGreyscaleImage(byte[] data) {
        Objects.requireNonNull(data, String.format("The byte array \"%s\" cannot be null.", "data"));
        int length = (int) Math.sqrt(data.length);
        BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        int index = 0;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                raster.setPixel(i, j, new int[] { data[index++] });
            }
        }

        return image;
    }
    //endregion

    //region Normalization

    /**
     * Normalizes the provided {@code int} array into a {@code byte} array. This is an O(n) operation.
     * @param data The {@code int} array
     * @return A {@code byte} array where each item is the normalized equivalent of the {@code int} value in the same
     * position as the input {@code int} array.
     * @throws NullPointerException If the {@code int} array is {@code null}.
     * @see #normalize(int)
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static byte[] normalize(int[] data) {
        Objects.requireNonNull(data, String.format("The int array \"%s\" cannot be null.", "data"));
        byte[] bytes = new byte[data.length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = normalize(data[i]);
        }

        return bytes;
    }

    /**
     * Normalizes the provided {@code int} value as a {@code byte} value.
     * @param value The {@code int} value.
     * @return The {@code int} value normalized as a {@code byte} value.
     * @see #normalize(int[])
     */
    @SuppressWarnings("WeakerAccess")
    @Contract(pure = true)
    public static byte normalize(int value)
    {
        double minInteger = Integer.MIN_VALUE;
        double maxInteger = Integer.MAX_VALUE;
        return (byte) ((value - minInteger) / (maxInteger - minInteger) * (Byte.MAX_VALUE - Byte.MIN_VALUE) + Byte.MIN_VALUE);
    }
    //endregion

    // region Upload to AWS S3

    /**
     * Uploads the provided {@link BufferedImage} to AWS S3.
     * @param image The {@link BufferedImage} to upload.
     * @param region The region to upload the {@link BufferedImage} to.
     * @param bucketName The name of the S3 bucket to upload the image to.
     * @param imageName The name of the image to upload to S3.
     * @throws IOException If an I/O error occurred.
     * @throws SdkClientException If any of the following occurred:
     * <ul>
     *     <li>
     *         The call was transmitted successfully, but Amazon S3 couldn't process it, so it returned an error
     *         response,
     *     </li>
     *     <li>Amazon S3 couldn't be contacted for a response, or</li>
     *     <li>The client couldn't parse the response from S3.</li>
     * </ul>
     */
    public static void uploadToS3(BufferedImage image, Regions region, String bucketName, String imageName) throws IOException, SdkClientException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        //This code expects that you have AWS credentials set up per:
        // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        // Upload a file as a new object with ContentType and title specified.

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.addUserMetadata("x-amz-meta-title", imageName);
        PutObjectRequest request = new PutObjectRequest(bucketName, imageName, inputStream, metadata);

        s3Client.putObject(request);
    }

    // endregion Upload to AWS S3
}
