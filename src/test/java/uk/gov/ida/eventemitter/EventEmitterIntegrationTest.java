package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.ida.eventemitter.utils.AmazonHelper;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestDecrypter;
import uk.gov.ida.eventemitter.utils.TestEvent;
import uk.gov.ida.eventemitter.utils.TestEventEmitterModule;

import java.nio.ByteBuffer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterIntegrationTest extends EventEmitterBaseConfiguration {
    private static final boolean CONFIGURATION_ENABLED = true;

    @BeforeClass
    public static void setUp() {
        AWSKMS awsKms = mock(AWSKMS.class);
        DecryptResult decryptResult = mock(DecryptResult.class);
        when(awsKms.decrypt(any(DecryptRequest.class))).thenReturn(decryptResult);
        when(decryptResult.getPlaintext()).thenReturn(ByteBuffer.wrap(KEY.getBytes()));
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            @Singleton
            private Configuration getConfiguration() {
                return new TestConfiguration(
                    CONFIGURATION_ENABLED,
                    ACCESS_KEY_ID,
                    ACCESS_SECRET_KEY,
                    Regions.EU_WEST_2,
                    SOURCE_QUEUE_NAME,
                    BUCKET_NAME,
                    KEY_NAME);
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule(awsKms)));

        sqs = AmazonHelper.getInstanceOfAmazonSqs(injector);
        s3 = AmazonHelper.getInstanceOfAmazonS3(injector);
        AmazonHelper.createSourceQueue(sqs, SOURCE_QUEUE_NAME);
        AmazonHelper.setUpS3Bucket(s3, BUCKET_NAME, KEY_NAME, KEY);
        queueUrl = AmazonHelper.getQueueUrl(injector);
    }

    @AfterClass
    public static void tearDown() {
        sqs.deleteQueue(queueUrl);
        AmazonHelper.deleteBucket(s3, BUCKET_NAME);
    }

    @Test
    public void shouldEncryptMessageUsingEventEncrypterAndSendToSQS() throws Exception {
        final EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        final Event event = aTestEventMessage().build();

        eventEmitter.record(event);

        final Message message = AmazonHelper.getAMessageFromSqs(sqs, queueUrl);
        sqs.deleteMessage(queueUrl, message.getReceiptHandle());
        final TestDecrypter<TestEvent> decrypter = new TestDecrypter(KEY.getBytes(), injector.getInstance(ObjectMapper.class));
        final Event actualEvent = decrypter.decrypt(message.getBody(), TestEvent.class);

        assertThat(actualEvent).isEqualTo(event);
    }
}
