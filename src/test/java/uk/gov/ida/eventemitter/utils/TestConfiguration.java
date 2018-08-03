package uk.gov.ida.eventemitter.utils;

import com.amazonaws.regions.Regions;
import uk.gov.ida.eventemitter.Configuration;

public final class TestConfiguration implements Configuration {

    private final boolean enabled;
    private final String accessKeyId;
    private final String accessSecretKey;
    private final Regions region;
    private final String queueAccountId;
    private final String sourceQueueName;
    private final String bucketName;
    private final String keyName;

    public TestConfiguration(
        final boolean enabled,
        final String accessKeyId,
        final String accessSecretKey,
        final Regions region,
        final String queueAccountId,
        final String sourceQueueName,
        final String bucketName,
        final String keyName) {

        this.enabled = enabled;
        this.accessKeyId = accessKeyId;
        this.accessSecretKey = accessSecretKey;
        this.region = region;
        this.queueAccountId = queueAccountId;
        this.sourceQueueName = sourceQueueName;
        this.bucketName = bucketName;
        this.keyName = keyName;
    }

    @Override
    public boolean isEnabled() { return enabled; }

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

    @Override
    public String getQueueAccountId() {
        return queueAccountId;
    }
}
