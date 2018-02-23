package uk.gov.ida.eventemitter;

public class TestConfiguration implements Configuration {

    private final String sourceQueueName;

    public TestConfiguration(final String sourceQueueName) {
        this.sourceQueueName = sourceQueueName;
    }

    @Override
    public String getSourceQueueName() {
        return sourceQueueName;
    }
}
