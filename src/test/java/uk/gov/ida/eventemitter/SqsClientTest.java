package uk.gov.ida.eventemitter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsClientTest {

    private static final String QUEUE_URL = "queueUrl";
    private static final String EVENT = "event";
    private static final SendMessageRequest SEND_MESSAGE_REQUEST = new SendMessageRequest(QUEUE_URL, EVENT);

    @Mock
    private AmazonSQS sqs;

    private SqsClient sqsClient;

    @Before
    public void setUp() {
        sqsClient = new SqsClient(sqs, QUEUE_URL);
    }

    @Test
    public void shouldSendEventToSqs() {
        final SendMessageResult result = new SendMessageResult();
        when(sqs.sendMessage(SEND_MESSAGE_REQUEST)).thenReturn(result);

        sqsClient.sendToSqs(EVENT);

        verify(sqs).sendMessage(SEND_MESSAGE_REQUEST);
    }

    @Test
    public void shouldLogErrorAfterFailingToSendAMessageToSqs() throws IOException {
        doThrow(new AmazonClientException("SQS is down")).when(sqs).sendMessage(SEND_MESSAGE_REQUEST);

        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(errorContent)) {
            System.setErr(printStream);
            sqsClient.sendToSqs(EVENT);
            System.setErr(System.err);

            assertThat(errorContent.toString()).contains("Failed to send the message to the queue. Error Message: SQS is down");
        }
    }
}
