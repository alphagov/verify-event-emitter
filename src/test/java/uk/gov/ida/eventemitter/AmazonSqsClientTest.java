package uk.gov.ida.eventemitter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonSqsClientTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";
    private static final String ENCRYPTED_EVENT = "encryptedEvent";
    private static final String QUEUE_URL = "queueUrl";
    private static final SendMessageRequest SEND_MESSAGE_REQUEST = new SendMessageRequest(QUEUE_URL, ENCRYPTED_EVENT);

    @Mock
    private AmazonSQS sqs;

    private AmazonSqsClient sqsClient;
    private Event event;

    @Before
    public void setUp() {
        sqsClient = new AmazonSqsClient(sqs, QUEUE_URL);
    }

    @Test
    public void shouldSendEventToSqs() {
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE, details);
        final SendMessageResult result = new SendMessageResult();
        when(sqs.sendMessage(SEND_MESSAGE_REQUEST)).thenReturn(result);

        sqsClient.send(event, ENCRYPTED_EVENT);

        verify(sqs).sendMessage(SEND_MESSAGE_REQUEST);
    }
}
