package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest {

    private static final String KEY = "aesEncryptionKey";
    private static final String INIT_VECTOR = "encryptionIntVec";

    private static Injector injector;
    private static String queueUrl;
    private static AmazonSQS clientSqs;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() { }

            @Provides
            @Singleton
            private AmazonSQS getAmazonSqs() {
                return TestUtils.getClientSQS();
            }

            @Provides
            @Singleton
            @Named("EventQueueUrl")
            public String getEventEmitter(AmazonSQS sqs) {
                return sqs.createQueue(new CreateQueueRequest("queueName")).getQueueUrl();
            }

            @Provides
            @Singleton
            private SqsClient getSqsClient(AmazonSQS sqs, @Named("EventQueueUrl") String eventQueueUrl) {
                return new SqsClient(sqs, eventQueueUrl);
            }

            @Provides
            @Singleton
            private Encrypter getEncrypter(ObjectMapper objectMapper) {
                return new LocalEncrypter(KEY, INIT_VECTOR, objectMapper);
            }

            @Provides
            @Singleton
            private ObjectMapper getObjectMapper() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JodaModule());
                return mapper;
            }

            @Provides
            @Singleton
            private EventEmitter getEventEmitter(Encrypter encrypter, SqsClient client) {
                return new EventEmitter(true, encrypter, client);
            }
        });
        clientSqs = injector.getInstance(AmazonSQS.class);
        queueUrl = injector.getInstance(Key.get(String.class, Names.named("EventQueueUrl")));
    }

    @AfterClass
    public static void tearDown() {
        clientSqs.deleteQueue(queueUrl);
    }

    @Test
    public void shouldEncryptMessageAndSendToSQS() throws Exception {
        final Event expectedEvent = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType");

        EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        eventEmitter.record(expectedEvent);

        final Message message = getAnEncryptedMessageFromSqs();
        clientSqs.deleteMessage(queueUrl, message.getReceiptHandle());
        final LocalDecrypter<TestEvent> decrypter = new LocalDecrypter(KEY, INIT_VECTOR, injector.getInstance(ObjectMapper.class));
        final Event actualEvent = decrypter.decrypt(message.getBody(), TestEvent.class);

        assertThat(actualEvent).isEqualTo(expectedEvent);
    }

    private Message getAnEncryptedMessageFromSqs() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        final List<Message> messages = clientSqs.receiveMessage(receiveMessageRequest).getMessages();

        assertThat(messages).hasSize(1);

        return messages.get(0);
    }
}
