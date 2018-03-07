package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest {

    private static final String KEY = "aesEncryptionKey";
    private static final String SOURCE_QUEUE_NAME = "sourceQueueName";
    private static final String BUCKET_NAME = "bucket.name";
    private static final String KEY_NAME = "keyName";
    private static Injector injector;
    private static Optional<String> queueUrl;
    private static Optional<AmazonSQS> sqs;
    private static Optional<AmazonS3> s3;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            private Optional<Configuration> getConfiguration() {
                return Optional.ofNullable(new TestConfiguration(SOURCE_QUEUE_NAME, BUCKET_NAME, KEY_NAME));
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule()));

        sqs = getInstanceOfAmazonSqs();
        s3 = getInstanceOfAmazonS3();
        createSourceQueue();
        setUpS3Bucket();
        queueUrl = getQueueUrl();
    }

    @AfterClass
    public static void tearDown() {
        sqs.get().deleteQueue(queueUrl.get());
        deleteBucket();
    }

    @Test
    public void shouldEncryptMessageAndSendToSQS() throws Exception {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType", details);

        eventEmitter.record(event);

        final Message message = getAnEncryptedMessageFromSqs();
        sqs.get().deleteMessage(queueUrl.get(), message.getReceiptHandle());
        final TestDecrypter<TestEvent> decrypter = new TestDecrypter(KEY.getBytes(), injector.getInstance(ObjectMapper.class));
        final Event actualEvent = decrypter.decrypt(message.getBody(), TestEvent.class);

        assertThat(actualEvent).isEqualTo(event);
    }

    private Message getAnEncryptedMessageFromSqs() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl.get());
        final List<Message> messages = sqs.get().receiveMessage(receiveMessageRequest).getMessages();

        assertThat(messages.size()).isEqualTo(1);
        return messages.get(0);
    }

    private static void createSourceQueue() {
        try {
            sqs.get().createQueue(SOURCE_QUEUE_NAME);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
    }

    private static void setUpS3Bucket() {
        try {
            s3.get().createBucket(BUCKET_NAME);
        } catch (AmazonS3Exception e) {
            System.err.println(e.getErrorMessage());
        }

        try {
            s3.get().putObject(BUCKET_NAME, KEY_NAME, KEY);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    private static void deleteBucket() {
        try {
            ObjectListing object_listing = s3.get().listObjects(BUCKET_NAME);
            while (true) {
                for (Iterator<?> iterator =
                     object_listing.getObjectSummaries().iterator();
                     iterator.hasNext();) {
                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                    s3.get().deleteObject(BUCKET_NAME, summary.getKey());
                }

                if (object_listing.isTruncated()) {
                    object_listing = s3.get().listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            };
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    private static Optional<String> getQueueUrl() {
        return (Optional<String>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, String.class)), Names.named("SourceQueueUrl")));
    }

    private static Optional<AmazonSQS> getInstanceOfAmazonSqs() {
        return (Optional<AmazonSQS>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, AmazonSQS.class))));
    }

    private static Optional<AmazonS3> getInstanceOfAmazonS3() {
        return (Optional<AmazonS3>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, AmazonS3.class))));
    }
}
