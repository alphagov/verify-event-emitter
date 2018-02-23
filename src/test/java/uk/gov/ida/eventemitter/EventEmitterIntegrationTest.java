package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.google.inject.util.Types;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.TestEventEmitterModule.INIT_VECTOR;
import static uk.gov.ida.eventemitter.TestEventEmitterModule.KEY;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest {

    private static final String QUEUE_NAME = "queueName";
    private static Injector injector;
    private static Optional<String> queueUrl;
    private static Optional<AmazonSQS> clientSqs;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            private Optional<Configuration> getConfiguration() {
                return Optional.ofNullable(new TestConfiguration(QUEUE_NAME));
            }
        }, Modules.override(new EventEmitterModule())
            .with(new TestEventEmitterModule()));
        clientSqs = (Optional<AmazonSQS>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, AmazonSQS.class))));
        clientSqs.get().createQueue(new CreateQueueRequest(QUEUE_NAME));
        queueUrl = (Optional<String>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, String.class)), Names.named("QueueUrl")));
    }

    @AfterClass
    public static void tearDown() {
        clientSqs.get().deleteQueue(queueUrl.get());
    }

    @Test
    public void shouldEncryptMessageAndSendToSQS() throws Exception {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType", details);

        eventEmitter.record(event);

        final Message message = getAnEncryptedMessageFromSqs();
        clientSqs.get().deleteMessage(queueUrl.get(), message.getReceiptHandle());
        final TestDecrypter<TestEvent> decrypter = new TestDecrypter(KEY, INIT_VECTOR, injector.getInstance(ObjectMapper.class));
        final Event actualEvent = decrypter.decrypt(message.getBody(), TestEvent.class);

        assertThat(actualEvent).isEqualTo(event);
    }

    private Message getAnEncryptedMessageFromSqs() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl.get());
        final List<Message> messages = clientSqs.get().receiveMessage(receiveMessageRequest).getMessages();

        assertThat(messages.size()).isEqualTo(1);
        return messages.get(0);
    }
}
