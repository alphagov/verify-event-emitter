package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.ida.eventemitter.utils.AmazonHelper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

@RunWith(LocalstackTestRunner.class)
public class AmazonSqsClientIntegrationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String ENCRYPTED_EVENT = "encryptedEvent";
    private static final String QUEUE_NAME = "queueName";

    private static AmazonSQS sqs;
    private static AmazonSqsClient sqsClient;
    private static String queueUrl;
    private static Event event;

    @BeforeClass
    public static void setUp() {
        event = aTestEventMessage().build();

        sqs = TestUtils.getClientSQS();
        AmazonHelper.createSourceQueue(sqs, QUEUE_NAME);
        queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        sqsClient = new AmazonSqsClient(sqs, queueUrl);
    }

    @AfterClass
    public static void tearDown() {
        sqs.deleteQueue(queueUrl);
    }

    @Test
    public void shouldSendEventToSqs() {
        sqsClient = new AmazonSqsClient(sqs, queueUrl);

        sqsClient.send(event, ENCRYPTED_EVENT);

        final Message message = AmazonHelper.getAMessageFromSqs(sqs, queueUrl);
        sqs.deleteMessage(queueUrl, message.getReceiptHandle());

        assertThat(message.getBody()).isEqualTo(ENCRYPTED_EVENT);
    }

    @Test
    public void shouldThrowErrorWhenSendingMessageToSqsWhereQueueDoesNotExist() {
        expectedException.expect(AmazonSQSException.class);
        expectedException.expectMessage("Invalid request: MissingQueryParamRejection(QueueName), " +
            "MissingQueryParamRejection(QueueUrl); see the SQS docs. (Service: AmazonSQS; Status Code: 400; Error " +
            "Code: Invalid request: MissingQueryParamRejection(QueueName), MissingQueryParamRejection(QueueUrl); " +
            "Request ID: 00000000-0000-0000-0000-000000000000)");

        sqsClient = new AmazonSqsClient(sqs, "nonExistentQueueUrl");

        sqsClient.send(event, ENCRYPTED_EVENT);
    }
}
