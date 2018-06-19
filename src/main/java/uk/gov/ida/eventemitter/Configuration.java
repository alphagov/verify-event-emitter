package uk.gov.ida.eventemitter;

import com.amazonaws.regions.Regions;

public interface Configuration {

    String getAccessKeyId();

    String getSecretAccessKey();

    Regions getRegion();

    String getSourceQueueName();

    String getBucketName();

    String getKeyName();
}
