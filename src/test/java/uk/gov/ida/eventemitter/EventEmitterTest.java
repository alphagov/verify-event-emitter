package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventEmitterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";
    private static final String ENCRYPTED_EVENT = "encrypted event";

    private EventEmitter eventEmitter;
    private TestEvent event;

    @Mock
    private EventEncrypter eventEncrypter;

    @Mock
    private SqsClient sqsClient;

    @Before
    public void setUp() throws Exception {
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE, details);
        when(eventEncrypter.encrypt(event)).thenReturn(ENCRYPTED_EVENT);

        eventEmitter = new EventEmitter(eventEncrypter, sqsClient);
    }

    @Test
    public void shouldEncryptAndSendEncryptedEventToSqs() throws Exception {
        eventEmitter.record(event);

         verify(eventEncrypter).encrypt(event);
         verify(sqsClient).send(event, ENCRYPTED_EVENT);
    }

    @Test
    public void shouldLogErrorAfterFailingToEncrypt() throws Exception {
        final String errorMessage = "Failed to encrypt.";
        when(eventEncrypter.encrypt(event)).thenThrow(new RuntimeException(errorMessage));

        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(errorContent)) {
            System.setErr(printStream);
            eventEmitter.record(event);
            System.setErr(System.err);

            assertThat(errorContent.toString()).contains(String.format(
                "Failed to send a message [Event Id: %s] to the queue. Error Message: %s\nEvent Message: null\n",
                ID,
                errorMessage
            ));
        }
    }

    @Test
    public void shouldLogErrorWhenEventIsNull() throws IOException {
        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(errorContent)) {
            System.setErr(printStream);
            eventEmitter.record(null);
            System.setErr(System.err);

            assertThat(errorContent.toString()).contains("Unable to send a message due to event containing null value.\n");
        }
    }
}
