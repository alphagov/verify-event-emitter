package uk.gov.ida.eventemitter;

import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.util.Optional;

public class TestEventEmitterModule extends AbstractModule {

    private Configuration configuration;

    public TestEventEmitterModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs() {
        if (this.configuration.getQueueName() != null) {
            return Optional.ofNullable(TestUtils.getClientSQS());
        }
        return Optional.empty();
    }
}
