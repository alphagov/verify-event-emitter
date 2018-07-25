package uk.gov.ida.eventemitter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

@RunWith(MockitoJUnitRunner.class)
public class AmazonSqsClientTest {

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
        event = aTestEventMessage().build();
        final SendMessageResult result = new SendMessageResult();
        when(sqs.sendMessage(SEND_MESSAGE_REQUEST)).thenReturn(result);

        sqsClient.send(event, ENCRYPTED_EVENT);

        verify(sqs).sendMessage(SEND_MESSAGE_REQUEST);
    }
}
