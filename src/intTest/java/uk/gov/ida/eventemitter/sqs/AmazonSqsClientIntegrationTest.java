package uk.gov.ida.eventemitter.sqs;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import cloud.localstack.docker.LocalstackDockerTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.ida.eventemitter.AuditEvent;
import uk.gov.ida.eventemitter.utils.TestEventBuilder;

@RunWith(LocalstackTestRunner.class)
public class AmazonSqsClientIntegrationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String QUEUE_NAME = "queueName";

    private static AmazonSQS sqs;
    private static AmazonSqsClient sqsClient;
    private static String queueUrl;
    private static AuditEvent event;
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() {
        event = TestEventBuilder.newInstance().build();
        objectMapper = new ObjectMapper();
        sqs = TestUtils.getClientSQS();
        AmazonSQSTestHelper.createSourceQueue(sqs, QUEUE_NAME);
        queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
    }

    @AfterClass
    public static void tearDown() {
        sqs.deleteQueue(queueUrl);
    }

    @Test
    public void shouldSendEventToSqs() throws JsonProcessingException {
        sqsClient = new AmazonSqsClient(sqs, queueUrl, objectMapper);

        sqsClient.send(event);

        final Message message = AmazonSQSTestHelper.getAMessageFromSqs(sqs, queueUrl);
        sqs.deleteMessage(queueUrl, message.getReceiptHandle());

        AssertionsForClassTypes.assertThat(message.getBody()).isEqualTo(objectMapper.writeValueAsString(event));
    }

    @Test
    public void shouldThrowErrorWhenSendingMessageToSqsWhereQueueDoesNotExist() {
        expectedException.expect(AmazonSQSException.class);
        expectedException.expectMessage("Invalid request: MissingQueryParamRejection(QueueName), " +
            "MissingQueryParamRejection(QueueUrl); see the SQS docs. (Service: AmazonSQS; Status Code: 400; Error " +
            "Code: Invalid request: MissingQueryParamRejection(QueueName), MissingQueryParamRejection(QueueUrl); " +
            "Request ID: 00000000-0000-0000-0000-000000000000)");

        sqsClient = new AmazonSqsClient(sqs, "nonExistentQueueUrl", objectMapper);

        sqsClient.send(event);
    }
}
