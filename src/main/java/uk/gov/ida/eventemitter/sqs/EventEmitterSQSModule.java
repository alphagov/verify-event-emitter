package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import uk.gov.ida.eventemitter.Configuration;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

public class EventEmitterSQSModule extends AbstractModule {

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
