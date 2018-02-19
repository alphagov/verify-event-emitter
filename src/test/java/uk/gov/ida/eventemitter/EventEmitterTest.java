package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventEmitterTest {

    private static final String ENCRYPTED_EVENT = "encrypted event";
    private static final UUID ID = UUID.randomUUID();
    private static final String TIMESTAMP = "2018-02-06T14:37:55.467Z";
    private static final String EVENT_TYPE = "Error Event";

    private EventEmitter eventEmitter;
    private EventEmitter eventEmitterWithoutRecordingSystem;
    private TestEvent event;

    @Mock
    private Encrypter encrypter;

    @Mock
    private SqsClient sqsClient;

    @Before
    public void setUp() throws Exception {
        when(encrypter.encrypt(any(Event.class))).thenReturn(ENCRYPTED_EVENT);

        eventEmitter = new EventEmitter(true, encrypter, sqsClient);
        eventEmitterWithoutRecordingSystem = new EventEmitter(false, encrypter, sqsClient);

        event = new TestEvent(ID, DateTime.parse(TIMESTAMP), EVENT_TYPE);
    }

    @Test
    public void shouldLogEventDetailsWhenEventEmitterIsConfiguredToSendToSqs() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            eventEmitter.record(event);
            System.setOut(System.out);

            assertThat(outContent.toString()).containsOnlyOnce(String.format(
                "Event ID: %s, Timestamp: %s, Event Type: %s, Event String: %s\n",
                ID,
                TIMESTAMP,
                EVENT_TYPE,
                ENCRYPTED_EVENT
            ));
        }
    }

    @Test
    public void shouldNotLogEventDetailsWhenEventEmitterIsConfiguredNotToSendToSqs() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            eventEmitterWithoutRecordingSystem.record(event);
            System.setOut(System.out);

            assertThat(outContent.toString()).isEmpty();
        }
    }

    @Test
    public void shouldNotThrowErrorsIfInputsAreNull() throws IOException {
        final TestEvent emptyEvent = new TestEvent(null, null, null);

        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(outContent)) {
            System.setOut(printStream);
            eventEmitter.record(emptyEvent);
            System.setOut(System.out);

            assertThat(outContent.toString()).containsOnlyOnce(String.format(
                "Event ID: null, Timestamp: null, Event Type: null, Event String: %s\n",
                ENCRYPTED_EVENT
            ));
        }
    }

    @Test
    public void shouldLogErrorToSystemErrorWhenEventEmitterIsUnableToSendToSqs() throws IOException {
        final String errorMessage = "Failed to send to SQS";
        doThrow(new RuntimeException(errorMessage)).when(sqsClient).sendToSqs(anyString());

        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(errorContent)) {
            System.setErr(printStream);
            eventEmitter.record(event);
            System.setErr(System.err);

            assertThat(errorContent.toString()).containsOnlyOnce(String.format(
                "Failed to record an event %s. Error message: %s\nEvent Message: %s",
                ID,
                errorMessage,
                ENCRYPTED_EVENT
            ));
        }
    }
}
