package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.regions.Regions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import uk.gov.ida.eventemitter.Configuration;
import uk.gov.ida.eventemitter.EventEmitterModule;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestEventEmitterModule;

import java.net.URI;

public class EventEmitterSQSTestHelper {

    protected static final String ACCESS_KEY_ID = System.getenv("AWS_DEV_ACCESS_KEY_ID");
    protected static final String ACCESS_SECRET_KEY = System.getenv("AWS_DEV_ACCESS_SECRET_KEY");
    protected static final byte[] KEY = "aesEncryptionKey".getBytes();
    protected static final String SOURCE_QUEUE_NAME = "default-doc-checking-event-recorder-queue";

    public static Injector createTestConfiguration(
            Boolean isEnabled,
            String accessKey,
            String secretAccessKey,
            Regions region,
            URI uri,
            String sourceQueueName,
            String queueAccountId
    ) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            @Singleton
            private Configuration getConfiguration() {
                return new TestConfiguration(
                        isEnabled,
                        accessKey,
                        secretAccessKey,
                        region,
                        uri,
                        KEY,
                        sourceQueueName
                );
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule()));
    }
}
