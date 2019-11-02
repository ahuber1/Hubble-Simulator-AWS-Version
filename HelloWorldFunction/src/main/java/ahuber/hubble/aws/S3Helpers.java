package ahuber.hubble.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

public final class S3Helpers {

    //region uploadImage

    /**
     * Uploads the provided {@link BufferedImage} to Amazon S3.
     * @param image The {@link BufferedImage} to upload.
     * @param location The location in Amazon S3 where the image will be uploaded to.
     * @throws NullPointerException If {@code location} is {@code null}
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
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     */
    public static PutObjectResult uploadImage(BufferedImage image, AmazonS3URI location)
            throws IOException, SdkClientException {

        return uploadImage(image,
                Regions.fromName(Objects.requireNonNull(location, "'location' cannot be null").getRegion()),
                location.getBucket(), location.getKey());
    }

    /**
     * Uploads the provided {@link BufferedImage} to Amazon S3.
     * @param image The {@link BufferedImage} to upload.
     * @param region The region to upload the {@link BufferedImage} to.
     * @param bucketName The name of the S3 bucket to upload the image to.
     * @param s3Key A key used to uniquely identify the content in S3.
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
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     */
    public static PutObjectResult uploadImage(BufferedImage image, Regions region, String bucketName, String s3Key)
            throws IOException, SdkClientException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return upload(inputStream, region, bucketName, s3Key, "image/jpeg");
    }
    //endregion

    //region uploadJson

    /**
     * Uploads the provided JSON string to Amazon S3
     * @param json The JSON to upload.
     * @param location The location in Amazon S3 where the JSON will be uploaded to.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     * @throws NullPointerException If {@code location} is {@code null}
     * @throws UnsupportedEncodingException If there was an error encoding the JSON string as a {@link StringInputStream}
     */
    public static PutObjectResult uploadJson(String json, AmazonS3URI location) throws UnsupportedEncodingException {
        return uploadJson(json,
                Regions.fromName(Objects.requireNonNull(location, "'location' cannot be null.").getRegion()),
                location.getBucket(), location.getKey());
    }

    /**
     * Uploads the provided JSON string to Amazon S3
     * @param json The JSON to upload.
     * @param region The region where the JSON will be uploaded
     * @param bucketName The name of the S3 bucket the JSON will be uploaded
     * @param s3Key A key used to uniquely identify the content in S3.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     * @throws UnsupportedEncodingException If there was an error encoding the JSON string as a {@link StringInputStream}
     */
    @SuppressWarnings("UnusedReturnValue")
    public static PutObjectResult uploadJson(String json, Regions region, String bucketName, String s3Key) throws UnsupportedEncodingException {
        return upload(json, region, bucketName, s3Key, "application/json");
    }

    //endregion

    // region upload

    /**
     * Uploads the provided string content to Amazon S3.
     * @param content The string content to upload to Amazon S3.
     * @param location The location in Amazon S3 where the content will be uploaded to.
     * @param contentType The HTTP Content-Type header indicating the type of content that will be stored in S3.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     * @throws NullPointerException If {@code location} is {@code null}
     * @throws UnsupportedEncodingException If there was an error encoding the string content as a {@link StringInputStream}
     */
    public static PutObjectResult upload(String content, AmazonS3URI location, String contentType) throws UnsupportedEncodingException {
        return upload(content,
                Regions.fromName(Objects.requireNonNull(location, "'location' cannot be null.").getRegion()),
                location.getBucket(), location.getKey(), contentType);
    }

    /**
     * Uploads the provided string content to Amazon S3.
     * @param content The string content to upload to Amazon S3.
     * @param region The region where the string content will be uploaded.
     * @param bucketName The name of the S3 bucket the string content will be uploaded to.
     * @param s3Key A key used to uniquely identify the content in S3.
     * @param contentType The HTTP Content-Type header indicating the type of content that will be stored in S3.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     * @throws UnsupportedEncodingException If there was an error encoding the string content as a {@link StringInputStream}
     */
    @SuppressWarnings("WeakerAccess")
    public static PutObjectResult upload(String content, Regions region, String bucketName, String s3Key,
            String contentType) throws UnsupportedEncodingException {
        StringInputStream stringInputStream = new StringInputStream(content);
        return upload(stringInputStream, region,bucketName, s3Key, contentType);
    }

    /**
     * Uploads data contained in the provided {@link InputStream} to Amazon S3.
     * @param inputStream The {@link InputStream} containing the data to upload to Amazon S3.
     * @param location The location in Amazon S3 where the data will be uploaded to.
     * @param contentType The HTTP Content-Type header indicating the type of content that will be stored in Amazon S3.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     *         performed.
     * @throws NullPointerException If {@code location} is {@code null}
     */
    public static PutObjectResult upload(InputStream inputStream, AmazonS3URI location, String contentType) {
        return upload(inputStream,
                Regions.fromName(Objects.requireNonNull(location, "'location' cannot be null").getRegion()),
                location.getBucket(), location.getKey(), contentType);
    }

    /**
     * Uploads data contained in the provided {@link InputStream} to Amazon S3.
     * @param inputStream The {@link InputStream} containing the data to upload to Amazon S3.
     * @param region The region where the string content will be uploaded.
     * @param bucketName The name of the S3 bucket the string content will be uploaded to.
     * @param s3Key A key used to uniquely identify the content in S3.
     * @param contentType The HTTP Content-Type header indicating the type of content that will be stored in S3.
     * @return A {@link PutObjectResult} from the S3 SDK containing information about the upload that was just
     * performed.
     */
    @SuppressWarnings("WeakerAccess")
    public static PutObjectResult upload(InputStream inputStream, Regions region, String bucketName, String s3Key,
            String contentType) {
        AmazonS3 s3Client = getS3Client(region);

        // Upload a file as a new object with ContentType and title specified.
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.addUserMetadata("x-amz-meta-title", s3Key);
        PutObjectRequest request = new PutObjectRequest(bucketName, s3Key, inputStream, metadata);

        return s3Client.putObject(request);
    }

    // endregion upload

    // region Download

    public static S3Object download(Regions region, String bucketName, String key) {
        AmazonS3 s3Client = getS3Client(region);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        return s3Client.getObject(getObjectRequest);
    }

    // endregion Download

    /**
     * Creates and returns an {@link AmazonS3} client for uploading files to the specified AWS Region to Amazon S3.
     * @param region The AWS Region.
     * @return The {@link AmazonS3} client for uploading files to the specified AWS Region to Amazon S3.
     */
    @SuppressWarnings("WeakerAccess")
    public static AmazonS3 getS3Client(Regions region) {
        //This code expects that you have AWS credentials set up per:
        // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    // region Urls

    public static String createS3Uri(String bucket, String path) {
        return String.format("s3://%s/%s", bucket, path);
    }

    public static String createHadoopLogFolderUri(String satelliteName) {
        return createS3Uri("hadoop_logs", String.format("java/%s/", satelliteName));
    }

    public static String createSparkJobConfigUri(String satelliteName) {
        return createS3Uri("spark_job_configs", String.format("java/%s.json", satelliteName));
    }

    public static String createSparkJobJarUri(String jarPath) {
        return createS3Uri("danshi", jarPath);
    }

    public static String createImageUri(String satelliteName) {
        return createS3Uri("satellite_images", String.format("java/%s.jpg", satelliteName));
    }

    // endregion Urls
}
