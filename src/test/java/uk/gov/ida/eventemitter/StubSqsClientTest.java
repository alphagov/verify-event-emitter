package uk.gov.ida.eventemitter;

import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.eventemitter.utils.TestEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

public class StubSqsClientTest {

    private static final String ENCRYPTED_EVENT = "encryptedEvent";

    private StubSqsClient sqsClient;
    private Event event;

    @Before
    public void setUp() {
        sqsClient = new StubSqsClient();
    }

    @Test
    public void shouldWriteEventDetailsToStandardOutput() throws IOException {
        event = aTestEventMessage().build();

        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            sqsClient.send(event, ENCRYPTED_EVENT);
            System.setOut(System.out);

            assertThat(outContent.toString())
                .containsOnlyOnce(String.format(
                "Event ID: %s, Timestamp: %s, Event Type: %s, Event String: %s\n",
                event.getEventId().toString(),
                event.getTimestamp(),
                event.getEventType(),
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
