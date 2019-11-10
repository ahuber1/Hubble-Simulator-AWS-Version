package ahuber.hubble.aws;

import ahuber.hubble.utils.PublicApi;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3ObjectId;
import com.amazonaws.services.s3.model.S3ObjectIdBuilder;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * An {@link S3ObjectId} that also contains the {@linkplain Regions region} of the S3 bucket.
 */
public class LocalizedS3ObjectId extends S3ObjectId {
    private final Regions region;

    /**
     * Creates a new {@link LocalizedS3ObjectId} that corresponds to an S3 object in the specified region and bucket,
     * and that has the specified key.
     *
     * @param region The region of the S3 bucket.
     * @param bucket The S3 bucket.
     * @param key    The key of the S3 object.
     */
    @SuppressWarnings("WeakerAccess")
    @PublicApi
    public LocalizedS3ObjectId(Regions region, String bucket, String key) {
        super(bucket, key);
        this.region = region;
    }

    /**
     * Creates a new {@link LocalizedS3ObjectId} that corresponds to an S3 object in the specified region and bucket,
     * and that has the specified key and version ID.
     *
     * @param region    The region of the S3 bucket.
     * @param bucket    The S3 bucket.
     * @param key       The key of the S3 object.
     * @param versionId The version ID of the S3 object.
     */
    @SuppressWarnings("WeakerAccess")
    @PublicApi
    public LocalizedS3ObjectId(Regions region, String bucket, String key, String versionId) {
        super(bucket, key, versionId);
        this.region = region;
    }

    /**
     * Creates a new {@link LocalizedS3ObjectId} that corresponds to an S3 object in the specified region and that is
     * contained in the bucket, has the specified key, and has additional parameters specified in the provided
     * {@link S3ObjectIdBuilder}
     *
     * @param region  The region of the S3 bucket.
     * @param builder The {@link S3ObjectIdBuilder}
     */
    @SuppressWarnings("unused")
    @PublicApi
    public LocalizedS3ObjectId(Regions region, S3ObjectIdBuilder builder) {
        super(builder);
        this.region = region;
    }

    /**
     * Creates a new {@link LocalizedS3ObjectId} that corresponds to an S3 object in the specified region and that is
     * contained in the bucket, has the key, and has additional parameters specified in the provided {@link S3ObjectId}
     *
     * @param region The region of the S3 bucket.
     * @param id     The {@link S3ObjectId} that contains additional parameters about the S3 object.
     * @throws NullPointerException If {@code id} is {@code null}
     */
    @SuppressWarnings("WeakerAccess")
    @PublicApi
    public LocalizedS3ObjectId(Regions region, S3ObjectId id) {
        this(region, Objects.requireNonNull(id, "'id' cannot be null.").getBucket(), id.getKey(), id.getVersionId());
    }

    /**
     * A copy constructor for {@link LocalizedS3ObjectId}
     *
     * @param id The {@link LocalizedS3ObjectId} from which to copy information regarding an S3 object in a
     *           particular region from.
     */
    @SuppressWarnings("unused")
    @PublicApi
    public LocalizedS3ObjectId(LocalizedS3ObjectId id) {
        this(Objects.requireNonNull(id, "'id' cannot be null.").getRegion(), id);
    }

    /**
     * Gets the region where this S3 object is located.
     *
     * @return The region where this S3 object is located.
     */
    @SuppressWarnings("WeakerAccess")
    @PublicApi
    public Regions getRegion() {
        return region;
    }

    /**
     * Creates and returns a URI string in the <i>s3://&lt;bucket&gt;/&lt;key&gt;</i> format that points to the S3
     * object referenced in this {@link LocalizedS3ObjectId}
     *
     * @return A URI string in the <i>s3://&lt;bucket&gt;/&lt;key&gt;</i> format.
     */
    @SuppressWarnings("WeakerAccess")
    @PublicApi
    public String getStringUri() {
        return S3Helpers.createS3Uri(getBucket(), getKey());
    }

    /**
     * Creates and returns an {@link AmazonS3URI} in the <i>s3://&lt;bucket&gt;/&lt;key&gt;</i> format that points to
     * the S3 object referenced in this {@link LocalizedS3ObjectId}
     *
     * @return An {@link AmazonS3URI} in the <i>s3://&lt;bucket&gt;/&lt;key&gt;</i> format.
     */
    public AmazonS3URI getUri() {
        return new AmazonS3URI(getStringUri());
    }

    /**
     * Determines whether the provided object is an {@link LocalizedS3ObjectId} and, if it is, whether it is
     * logically equivalent to this {@link LocalizedS3ObjectId}
     *
     * @param o The object to compare against this {@link LocalizedS3ObjectId} instance for logical equality.
     * @return {@code true} if {@code o} is a {@link LocalizedS3ObjectId} and is logically equivalent to this
     * {@link LocalizedS3ObjectId} instance.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalizedS3ObjectId)) {
            return false;
        }

        LocalizedS3ObjectId that = (LocalizedS3ObjectId) o;
        return getRegion() == that.getRegion() &&
                Objects.equals(getBucket(), that.getBucket()) &&
                Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getVersionId(), that.getVersionId());
    }

    /**
     * Computes the hashcode for this {@link LocalizedS3ObjectId} instance.
     *
     * @return The hashcode for this {@link LocalizedS3ObjectId} instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getRegion(), getBucket(), getKey(), getVersionId());
    }
}
