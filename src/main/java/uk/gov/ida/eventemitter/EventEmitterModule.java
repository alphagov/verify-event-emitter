package uk.gov.ida.eventemitter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import java.util.Optional;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs(final Optional<Configuration> configuration) {
        if (configuration.isPresent() && configuration.get().getSourceQueueName() != null) {
            return Optional.ofNullable(AmazonSQSClientBuilder.defaultClient());
        }
        return Optional.empty();
    }

    @Provides
    @Named("SourceQueueUrl")
    private Optional<String> getQueueUrl(final Optional<AmazonSQS> amazonSqs,
                                         final Optional<Configuration> configuration) {
        return amazonSqs.map(sqs -> sqs.getQueueUrl(configuration.get().getSourceQueueName()).getQueueUrl());
    }

    @Provides
    private SqsClient getAmazonSqsClient(final Optional<AmazonSQS> amazonSqs,
                                         final @Named("SourceQueueUrl") Optional<String> sourceQueueUrl) {
        if (amazonSqs.isPresent() && sourceQueueUrl.isPresent()){
            return new AmazonSqsClient(amazonSqs.get(), sourceQueueUrl.get());
        }
        return new StubSqsClient();
    }

    @Provides
    private EventEmitter getEventEmitter(final SqsClient sqsClient) {
        return new EventEmitter(new StubEncrypter(), sqsClient);
    }
}
