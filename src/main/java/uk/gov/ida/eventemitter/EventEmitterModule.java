package uk.gov.ida.eventemitter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import java.util.Optional;

public class EventEmitterModule extends AbstractModule {

    private Configuration configuration;

    public EventEmitterModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs() {
        if (this.configuration.getQueueName() != null) {
            return Optional.ofNullable(AmazonSQSClientBuilder.defaultClient());
        }
        return Optional.empty();
    }

    @Provides
    @Named("QueueUrl")
    private Optional<String> getQueueUrl(final Optional<AmazonSQS> amazonSqs) {
        return amazonSqs.map(sqs -> sqs.getQueueUrl(configuration.getQueueName()).getQueueUrl());
    }

    @Provides
    private SqsClient getAmazonSqsClient(final Optional<AmazonSQS> amazonSqs,
                                         final @Named("QueueUrl") Optional<String> queueUrl) {
        if (amazonSqs.isPresent() && queueUrl.isPresent()){
            return new AmazonSqsClient(amazonSqs.get(), queueUrl.get());
        }
        return new StubSqsClient();
    }

    @Provides
    private EventEmitter getEventEmitter(final SqsClient sqsClient) {
        return new EventEmitter(new StubEncrypter(), sqsClient);
    }
}
