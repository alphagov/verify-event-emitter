package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest {

    private static final String QUEUE_NAME = "queueName";
    private static final TestConfiguration CONFIGURATION = new TestConfiguration(QUEUE_NAME);
    private static Injector injector;
    private static Optional<String> queueUrl;
    private static Optional<AmazonSQS> clientSqs;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(Modules.override(new EventEmitterModule(CONFIGURATION))
            .with(new TestEventEmitterModule(CONFIGURATION)));
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
    public void shouldEncryptMessageAndSendToSQS() {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType");

        eventEmitter.record(event);

        final Message message = getAnEncryptedMessageFromSqs();
        clientSqs.get().deleteMessage(queueUrl.get(), message.getReceiptHandle());

        assertThat(message.getBody()).isEqualTo(String.format("Encrypted Event Id %s", event.getEventId().toString()));
    }

    private Message getAnEncryptedMessageFromSqs() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl.get());
        final List<Message> messages = clientSqs.get().receiveMessage(receiveMessageRequest).getMessages();

        assertThat(messages.size()).isEqualTo(1);
        return messages.get(0);
    }
}
