package uk.gov.ida.eventemitter;

public interface Configuration {

    String getSourceQueueName();

    String getBucketName();

    String getKeyName();
}
