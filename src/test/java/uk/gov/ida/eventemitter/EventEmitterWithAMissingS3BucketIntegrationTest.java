package uk.gov.ida.eventemitter;

import cloud.localstack.LocalstackTestRunner;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.util.Modules;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.ida.eventemitter.utils.AmazonHelper;
import uk.gov.ida.eventemitter.utils.TestConfiguration;
import uk.gov.ida.eventemitter.utils.TestEventEmitterModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LocalstackTestRunner.class)
public class EventEmitterWithAMissingS3BucketIntegrationTest extends EventEmitterBaseConfiguration {
    private static final boolean CONFIGURATION_ENABLED = true;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
            private Configuration getConfiguration() {
                return new TestConfiguration(
                    CONFIGURATION_ENABLED,
                    ACCESS_KEY_ID,
                    ACCESS_SECRET_KEY,
                    Regions.EU_WEST_2,
                    QUEUE_ACCOUNT_ID,
                    SOURCE_QUEUE_NAME,
                    BUCKET_NAME,
                    KEY_NAME
                );
            }
        }, Modules.override(new EventEmitterModule()).with(new TestEventEmitterModule(awsKms)));

        sqs = AmazonHelper.getInstanceOfAmazonSqs(injector);
        s3 = AmazonHelper.getInstanceOfAmazonS3(injector);
        AmazonHelper.createSourceQueue(sqs, SOURCE_QUEUE_NAME);
        queueUrl = AmazonHelper.getQueueUrl(injector);
        System.setErr(System.err);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        sqs.deleteQueue(queueUrl);
        AmazonHelper.deleteBucket(s3, BUCKET_NAME);
        try {
            printStream.close();
        } finally {
            errorContent.close();
        }
    }

    @Test
    public void shouldThrowExceptionWhenS3BucketIsMissing() {
        expectedException.expect(ProvisionException.class);
        expectedException.expectMessage("Failed to load S3 bucket bucket.name");
        injector.getInstance(EventEmitter.class);
    }
}
