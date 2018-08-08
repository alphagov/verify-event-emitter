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
    @Named("SourceQueueUrl")
    private String getQueueUrl(
            @Nullable final AmazonSQS amazonSqs,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest(configuration.getSourceQueueName())
                    .withQueueOwnerAWSAccountId(configuration.getQueueAccountId());
            return amazonSqs.getQueueUrl(queueUrlRequest).getQueueUrl();
        }
        return "";
    }

    @Provides
    @Singleton
    @Named("EncryptionKey")
    private String getEncryptionKey(final Configuration configuration) {
        if (configuration.isEnabled()) {
             return configuration.getEncryptionKey();
        }
        return "";
    }

    @Provides
    @Singleton
    private SqsClient getAmazonSqsClient(
            @Nullable final AmazonSQS amazonSqs,
            final @Named("SourceQueueUrl") String sourceQueueUrl,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            return new AmazonSqsClient(amazonSqs, sourceQueueUrl);
        }
        return new StubSqsClient();
    }

    @Provides
    @Singleton
    private Encrypter getEncrypter(
            final Configuration configuration,
            final ObjectMapper mapper,
            final @Named("EncryptionKey") String encryptionKey) {
        if (configuration.isEnabled()) {
            return new EventEncrypter(encryptionKey.getBytes(), mapper);
        }
        return new StubEncrypter();
    }

    @Provides
    @Singleton
    private EventEmitter getEventEmitter(
            final SqsClient sqsClient,
            final Encrypter encrypter) {
        return new EventEmitter(encrypter, sqsClient);
    }
}
