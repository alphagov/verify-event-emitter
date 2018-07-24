package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.util.Modules;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestEvent;
import uk.gov.ida.eventemitter.utils.TestEventEmitterModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterWithDisabledConfigTest extends EventEmitterBaseConfiguration {
    private static final boolean CONFIGURATION_ENABLED = false;

    @BeforeClass
    public static void setUp() {
        AWSKMS awsKms = mock(AWSKMS.class);
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            private Configuration getConfiguration() {
                return new TestConfiguration(
                    CONFIGURATION_ENABLED,
                    ACCESS_KEY_ID,
                    ACCESS_SECRET_KEY,
                    Regions.EU_WEST_2,
                    SOURCE_QUEUE_NAME,
                    BUCKET_NAME,
                    KEY_NAME
                );
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule(awsKms)));
    }

    @Test
    public void shouldReturnStubbedClientWhenConfigIsDisabled() {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Map<String, String> details = new HashMap<>();
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType", details);

        eventEmitter.record(event);

        assertThat(eventEmitter.getClass().equals(StubSqsClient.class));
    }
}
