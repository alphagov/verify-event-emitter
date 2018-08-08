package uk.gov.ida.eventemitter;

import com.amazonaws.regions.Regions;

public interface Configuration {
    boolean isEnabled();

    String getAccessKeyId();

    String getSecretAccessKey();

    Regions getRegion();

    String getSourceQueueName();

    String getQueueAccountId();

    String getEncryptionKey();
}
