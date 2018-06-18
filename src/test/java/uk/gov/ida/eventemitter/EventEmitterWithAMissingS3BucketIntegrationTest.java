package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.util.Modules;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterWithAMissingS3BucketIntegrationTest {

    private static final String KEY = "aesEncryptionKey";
    private static final String SOURCE_QUEUE_NAME = "sourceQueueName";
    private static final String BUCKET_NAME = "bucket.name";
    private static final String KEY_NAME = "keyName";
    private static Injector injector;
    private static Optional<String> queueUrl;
    private static Optional<AmazonSQS> sqs;
    private static Optional<AmazonS3> s3;
    private static ByteArrayOutputStream errorContent;
    private static PrintStream printStream;

    @BeforeClass
    public static void setUp() {
        AWSKMS awsKms = mock(AWSKMS.class);
        DecryptResult decryptResult = mock(DecryptResult.class);
        when(awsKms.decrypt(any(DecryptRequest.class))).thenReturn(decryptResult);
        when(decryptResult.getPlaintext()).thenReturn(ByteBuffer.wrap(KEY.getBytes()));
        errorContent = new ByteArrayOutputStream();
        printStream = new PrintStream(errorContent);
        System.setErr(printStream);
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            private Optional<Configuration> getConfiguration() {
                return Optional.ofNullable(new TestConfiguration(SOURCE_QUEUE_NAME, BUCKET_NAME, KEY_NAME));
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule(awsKms)));

        sqs = AmazonHelper.getInstanceOfAmazonSqs(injector);
        s3 = AmazonHelper.getInstanceOfAmazonS3(injector);
        AmazonHelper.createSourceQueue(sqs.get(), SOURCE_QUEUE_NAME);
        queueUrl = AmazonHelper.getQueueUrl(injector);
        System.setErr(System.err);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        sqs.get().deleteQueue(queueUrl.get());
        AmazonHelper.deleteBucket(s3.get(), BUCKET_NAME);
        try {
            printStream.close();
        }
        finally {
            errorContent.close();
        }
    }

    @Test
    public void shouldEncryptMessageUsingStubEncrypterAndSendToSQSWhenS3BucketIsMissing() throws Exception {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        final Event event = new TestEvent(UUID.randomUUID(), DateTime.now(DateTimeZone.UTC), "eventType", details);

        eventEmitter.record(event);

        final Message message = AmazonHelper.getAMessageFromSqs(sqs.get(), queueUrl.get());
        sqs.get().deleteMessage(queueUrl.get(), message.getReceiptHandle());

        assertThat(errorContent.toString()).contains("Failed to load S3 bucket bucket.name.");
        assertThat(message.getBody()).isEqualTo(String.format("Encrypted Event Id %s", event.getEventId().toString()));
    }
}
