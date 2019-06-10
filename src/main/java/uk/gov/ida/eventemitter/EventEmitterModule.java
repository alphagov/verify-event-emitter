package uk.gov.ida.eventemitter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import uk.gov.ida.eventemitter.sqs.AmazonSqsClient;
import uk.gov.ida.eventemitter.sqs.EventEmitterSQS;
import uk.gov.ida.eventemitter.sqs.SqsClient;
import uk.gov.ida.eventemitter.sqs.StubSqsClient;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    @Nullable
    private AWSCredentials getAmazonCredential(final Configuration configuration) {
        if (configuration.isEnabled()) {
            return new BasicAWSCredentials(configuration.getAccessKeyId(), configuration.getSecretAccessKey());
        }
        return null;
    }

    @Provides
    @Nullable
    @Named("EncryptionKey")
    private byte[] getEncryptionKey(final Configuration configuration) {
        if (configuration.isEnabled()) {
             return configuration.getEncryptionKey();
        }
        return null;
    }

    @Provides
    @Singleton
    private EventSender getEventSender(
            final Configuration configuration,
            final @Nullable AWSCredentials credentials) {
        if (configuration.isEnabled()) {
            return new AmazonEventSender(configuration.getApiGatewayUrl(), credentials, configuration.getRegion());
        }
        return new StubEventSender();
    }

    @Provides
    @Singleton
    private Encrypter getEncrypter(
            final Configuration configuration,
            final @Nullable @Named("EncryptionKey") byte[] encryptionKey) {
        if (configuration.isEnabled()) {
            return new EventEncrypter(encryptionKey);
        }
        return new StubEncrypter();
    }

    @Provides
    @Singleton
    private EventEmitter getEventEmitter(
            final ObjectMapper objectMapper,
            final EventSender eventSender,
            final Encrypter encrypter) {
        return new EventEmitter(objectMapper, new EventHasher(new Sha256Util()), encrypter, eventSender);
    }

    @Provides
    @Singleton
    private EventEmitterSQS getEventEmitter(
            final SqsClient sqsClient) {
        return new EventEmitterSQS(sqsClient);
    }

    @Provides
    @Singleton
    private SqsClient getAmazonSqsClient(
            @Nullable final AmazonSQS amazonSqs,
            final @Named("SourceQueueUrl") String sourceQueueUrl,
            final Configuration configuration,
            final ObjectMapper objectMapper) {
        if (configuration.isEnabled()) {
            return new AmazonSqsClient(amazonSqs, sourceQueueUrl, objectMapper);
        }
        return new StubSqsClient();
    }

    @Provides
    @Singleton
    @Nullable
    private AmazonSQS getAmazonSqs(
            final Configuration configuration,
            @Nullable final AWSCredentials credentials) {
        if (configuration.isEnabled()) {
            return AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(configuration.getRegion())
                    .build();
        }
        return null;
    }

    @Provides
    @Singleton
    @Nullable
    @Named("SourceQueueUrl")
    private String getQueueUrl(
            @Nullable final AmazonSQS amazonSqs,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest(configuration.getSourceQueueName());
            return amazonSqs.getQueueUrl(queueUrlRequest).getQueueUrl();
        }
        return "";
    }

}
