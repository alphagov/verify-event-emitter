package uk.gov.ida.eventemitter.sqs;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;
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
@LocalstackDockerProperties(services = { "sqs" })
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
        expectedException.expectMessage("The specified queue does not exist for this wsdl version. " +
                "(Service: AmazonSQS; " +
                "Status Code: 404; " +
                "Error Code: AWS.SimpleQueueService.NonExistentQueue; " +
                "Request ID: ");

        sqsClient = new AmazonSqsClient(sqs, queueUrl + "notHere", objectMapper);

        sqsClient.send(event);
    }
}