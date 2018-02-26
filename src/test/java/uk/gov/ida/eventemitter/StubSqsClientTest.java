package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StubSqsClientTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";
    private static final String ENCRYPTED_EVENT = "encryptedEvent";

    private StubSqsClient sqsClient;
    private Event event;

    @Before
    public void setUp() {
        sqsClient = new StubSqsClient();
    }

    @Test
    public void shouldWriteEventDetailsToStandardOutput() throws IOException {
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE, details);

        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            sqsClient.send(event, ENCRYPTED_EVENT);
            System.setOut(System.out);

            assertThat(outContent.toString())
                .containsOnlyOnce(String.format(
                "Event ID: %s, Timestamp: %s, Event Type: %s, Event String: %s\n",
                ID,
                TIMESTAMP,
                EVENT_TYPE,
                ENCRYPTED_EVENT
            ));
        }
    }

    @Test
    public void shouldNotThrowErrorsIfInputsAreNull() throws IOException {
        event = new TestEvent(null, null, null, null);

        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            sqsClient.send(event, "null");
            System.setOut(System.out);

            assertThat(outContent.toString())
                .containsOnlyOnce("Event ID: null, Timestamp: null, Event Type: null, Event String: null\n");
        }
    }
}
