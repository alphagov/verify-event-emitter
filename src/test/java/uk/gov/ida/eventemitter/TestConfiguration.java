package uk.gov.ida.eventemitter;

public class TestConfiguration implements Configuration {

    private final String queueName;

    public TestConfiguration(final String queueName) {
        this.queueName = queueName;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }
}
