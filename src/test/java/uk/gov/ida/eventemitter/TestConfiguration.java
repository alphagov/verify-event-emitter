package uk.gov.ida.eventemitter;

public class TestConfiguration implements Configuration {

    private final String sourceQueueName;
    private final String bucketName;
    private final String keyName;

    public TestConfiguration(final String sourceQueueName,
                             final String bucketName,
                             final String keyName) {
        this.sourceQueueName = sourceQueueName;
        this.bucketName = bucketName;
        this.keyName = keyName;
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
