package uk.gov.ida.eventemitter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import httpstub.HttpStubRule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestDecrypter;
import uk.gov.ida.eventemitter.utils.TestEvent;
import uk.gov.ida.eventemitter.utils.TestEventEmitterModule;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

public class EventEmitterIntegrationTest extends EventEmitterTestBaseConfiguration {

    private static final boolean CONFIGURATION_ENABLED = true;
    private static TestAppender testAppender = new TestAppender();

    private enum HttpResponse {

        HTTP_200(200, "OK"),
        HTTP_403(403, "Forbidden"),
        HTTP_404(404, "Not Found"),
        HTTP_504(504, "Gateway Timeout");

        private int statusCode;
        private String statusText;

        HttpResponse(int statusCode, String statusText) {
            this.statusCode = statusCode;
            this.statusText = statusText;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusText() {
            return statusText;
        }
    }

    @ClassRule
    public static HttpStubRule apiGatewayStub = new HttpStubRule();

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

            }

            @Provides
            @Singleton
            private Configuration getConfiguration() {
                return new TestConfiguration(
                        CONFIGURATION_ENABLED,
                        ACCESS_KEY_ID,
                        ACCESS_SECRET_KEY,
                        Regions.EU_WEST_2,
                        URI.create(apiGatewayStub.baseUri().build().toString() + AUDIT_EVENTS_API_RESOURCE),
                        KEY);
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule()));
    }

    @Before
    public void setUpLogAppender() {
        testAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(testAppender);
    }

    @After
    public void tearDownLogAppender() throws Exception {
        testAppender.stop();
        TestAppender.events.clear();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(testAppender);
        apiGatewayStub.reset();
    }

    @Test
    public void shouldEncryptMessageUsingEventEncrypterAndSendToAPIGateway() throws Exception {


        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = aTestEventMessage().build();

        apiGatewayStub.register(AUDIT_EVENTS_API_RESOURCE, HttpResponse.HTTP_200.getStatusCode());

        eventEmitter.record(event);

        final String message = new String(apiGatewayStub.getLastRequest().getEntityBytes());
        final TestDecrypter<TestEvent> decrypter = new TestDecrypter(KEY, injector.getInstance(ObjectMapper.class));
        final Event actualEvent = decrypter.decrypt(message, TestEvent.class);

        assertThat(actualEvent).isEqualTo(event);
        assertThat(TestAppender.events.get(0).toString()).contains(
                String.format("Sent Event Message [Event Id: %s]",
                        event.getEventId()));
    }

    @Test
    public void shouldFailSilentlyWithIncorrectResource() throws Exception {

        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = aTestEventMessage().build();

        apiGatewayStub.register(AUDIT_EVENTS_API_RESOURCE_INVALID, HttpResponse.HTTP_404.getStatusCode());

        eventEmitter.record(event);

        assertThat(
                TestAppender.events.get(0).toString()).contains(
                String.format("Failed to send a message [Event Id: %s] to the queue. Status: %s Error Message: %s",
                        event.getEventId(),
                        HttpResponse.HTTP_404.getStatusCode(),
                        HttpResponse.HTTP_404.getStatusText()));

    }

    @Test
    public void shouldFailSilentlyWithUnauthorized() throws Exception {

        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = aTestEventMessage().build();

        apiGatewayStub.register(AUDIT_EVENTS_API_RESOURCE, HttpResponse.HTTP_403.getStatusCode());

        eventEmitter.record(event);

        assertThat(
                TestAppender.events.get(0).toString()).contains(
                String.format("Failed to send a message [Event Id: %s] to the queue. Status: %s Error Message: %s",
                        event.getEventId(),
                        HttpResponse.HTTP_403.getStatusCode(),
                        HttpResponse.HTTP_403.getStatusText()));

    }

    @Test
    public void shouldFailSilentlyWithTimeout() throws Exception {

        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = aTestEventMessage().build();

        apiGatewayStub.register(AUDIT_EVENTS_API_RESOURCE, HttpResponse.HTTP_504.getStatusCode());

        eventEmitter.record(event);

        assertThat(
                TestAppender.events.get(0).toString()).contains(
                String.format("Failed to send a message [Event Id: %s] to the queue. Status: %s Error Message: %s",
                        event.getEventId(),
                        HttpResponse.HTTP_504.getStatusCode(),
                        HttpResponse.HTTP_504.getStatusText()));

    }

    @Test(expected = Test.None.class)
    public void shouldFailSilentlyWithNullEvent() throws Exception {

        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);

        eventEmitter.record(null);

        assertThat(
                TestAppender.events.get(0).toString()).contains(
                "[ERROR] Unable to send a message due to event containing null value.");

    }

    private static class TestAppender extends AppenderBase<ILoggingEvent> {

        public static List<ILoggingEvent> events = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent eventObject) {

            if (eventObject.getLevel() != Level.DEBUG) {
                events.add(eventObject);
            }
        }

    }

}