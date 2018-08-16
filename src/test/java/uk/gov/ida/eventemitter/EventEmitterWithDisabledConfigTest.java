package uk.gov.ida.eventemitter;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import org.glassfish.jersey.client.JerseyClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestEvent;

import javax.ws.rs.client.Client;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class EventEmitterWithDisabledConfigTest extends EventEmitterTestBaseConfiguration {
    private static final boolean CONFIGURATION_ENABLED = false;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Client.class).to(JerseyClient.class);
            }

            @Provides
            private Configuration getConfiguration() {
                return new TestConfiguration(
                        CONFIGURATION_ENABLED,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }
        }, new EventEmitterModule());
    }

    @Test
    public void shouldReturnStubbedClientWhenConfigIsDisabled() {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Map<String, String> details = new HashMap<>();
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType", details);

        eventEmitter.record(event);

        assertThat(eventEmitter.getClass().equals(StubEventSender.class));
    }
}
