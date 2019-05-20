package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.eventemitter.AuditEvent;
import uk.gov.ida.eventemitter.utils.TestEventBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonSqsClientTest {

    private static final String QUEUE_URL = "queueUrl";

    @Mock
    private AmazonSQS sqs;

    private AmazonSqsClient sqsClient;
    private AuditEvent event;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        sqsClient = new AmazonSqsClient(sqs, QUEUE_URL, new ObjectMapper());
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldSendEventToSqs() throws JsonProcessingException
    {
        event = TestEventBuilder.newInstance().build();
        final SendMessageRequest SEND_MESSAGE_REQUEST =
                new SendMessageRequest(QUEUE_URL, objectMapper.writeValueAsString(event));
        final SendMessageResult result = new SendMessageResult();
        when(sqs.sendMessage(SEND_MESSAGE_REQUEST)).thenReturn(result);

        sqsClient.send(event);

        verify(sqs).sendMessage(SEND_MESSAGE_REQUEST);
    }
}
