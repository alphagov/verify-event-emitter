package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest {

    private static final String QUEUE_NAME = "queueName";
    private static Injector injector;
    private static String queueUrl;
    private static AmazonSQS clientSqs;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Encrypter.class).to(StubEncrypter.class).asEagerSingleton();
            }

            @Provides
            @Singleton
            private AmazonSQS getAmazonSqs() {
                return TestUtils.getClientSQS();
            }

            @Provides
            @Singleton
            @Named("QueueUrl")
            private String getQueueUrl() {
                return clientSqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
            }

            @Provides
            @Singleton
            private SqsClient getSqsClient(final AmazonSQS sqs,
                                           @Named("QueueUrl") final String queueUrl) {
                return new AmazonSqsClient(sqs, queueUrl);
            }

            @Provides
            @Singleton
            private EventEmitter getEventEmitter(Encrypter encrypter, SqsClient client) {
                return new EventEmitter(encrypter, client);
            }
        });
        clientSqs = injector.getInstance(AmazonSQS.class);
        clientSqs.createQueue(new CreateQueueRequest(QUEUE_NAME));
        queueUrl = injector.getInstance(Key.get(String.class, Names.named("QueueUrl")));
    }

    @AfterClass
    public static void tearDown() {
        clientSqs.deleteQueue(queueUrl);
    }

    @Test
    public void shouldEncryptMessageAndSendToSQS() {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType");

        eventEmitter.record(event);

        final Message message = getAnEncryptedMessageFromSqs();
        clientSqs.deleteMessage(queueUrl, message.getReceiptHandle());

        assertThat(message.getBody()).isEqualTo(String.format("Encrypted Event Id %s", event.getEventId().toString()));
    }

    private Message getAnEncryptedMessageFromSqs() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        final List<Message> messages = clientSqs.receiveMessage(receiveMessageRequest).getMessages();

        assertThat(messages.size()).isEqualTo(1);
        return messages.get(0);
    }
}
