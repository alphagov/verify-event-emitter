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
    private final byte[] encryptionKey;

    public TestConfiguration(
        final boolean enabled,
        final String accessKeyId,
        final String accessSecretKey,
        final Regions region,
        final String queueAccountId,
        final String sourceQueueName,
        final byte[] encryptionKey) {

        this.enabled = enabled;
        this.accessKeyId = accessKeyId;
        this.accessSecretKey = accessSecretKey;
        this.region = region;
        this.queueAccountId = queueAccountId;
        this.sourceQueueName = sourceQueueName;
        this.encryptionKey = encryptionKey;
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
    public String getQueueAccountId() {
        return queueAccountId;
    }

    @Override
    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

}
