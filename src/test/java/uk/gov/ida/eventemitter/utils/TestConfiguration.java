package uk.gov.ida.eventemitter.utils;

import com.amazonaws.regions.Regions;
import uk.gov.ida.eventemitter.Configuration;

public final class TestConfiguration implements Configuration {

    private final String accessKeyId;
    private final String accessSecretKey;
    private final Regions region;
    private final String sourceQueueName;
    private final String bucketName;
    private final String keyName;

    public TestConfiguration(
        final String accessKeyId,
        final String accessSecretKey,
        final Regions region,
        final String sourceQueueName,
        final String bucketName,
        final String keyName) {

        this.accessKeyId = accessKeyId;
        this.accessSecretKey = accessSecretKey;
        this.region = region;
        this.sourceQueueName = sourceQueueName;
        this.bucketName = bucketName;
        this.keyName = keyName;
    }

    @Override
    public String getAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getSecretAccessKey() {
        return accessSecretKey;
    }

    @Override
    public Regions getRegion() {
        return region;
    }

    @Override
    public String getSourceQueueName() {
        return sourceQueueName;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }
}
