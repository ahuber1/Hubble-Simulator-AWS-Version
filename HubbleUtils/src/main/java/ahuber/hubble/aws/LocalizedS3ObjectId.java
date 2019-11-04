package ahuber.hubble.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3ObjectId;
import com.amazonaws.services.s3.model.S3ObjectIdBuilder;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class LocalizedS3ObjectId extends S3ObjectId {
    private final Regions region;

    public LocalizedS3ObjectId(Regions region, String bucket, String key) {
        super(bucket, key);
        this.region = region;
    }

    public LocalizedS3ObjectId(Regions region, String bucket, String key, String versionId) {
        super(bucket, key, versionId);
        this.region = region;
    }

    public LocalizedS3ObjectId(Regions region, S3ObjectIdBuilder builder) {
        super(builder);
        this.region = region;
    }

    public LocalizedS3ObjectId(Regions region, S3ObjectId id) {
        this(region, Objects.requireNonNull(id, "'id' cannot be null.").getBucket(), id.getKey(), id.getVersionId());
    }

    public LocalizedS3ObjectId(LocalizedS3ObjectId id) {
        this(Objects.requireNonNull(id, "'id' cannot be null.").getRegion(), id);
    }

    public Regions getRegion() {
        return region;
    }

    public String getStringUri() {
        return S3Helpers.createS3Uri(getBucket(), getKey());
    }

    public AmazonS3URI getUri() {
        return new AmazonS3URI(getStringUri());
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(getRegion(), getBucket(), getKey(), getVersionId());
    }
}
