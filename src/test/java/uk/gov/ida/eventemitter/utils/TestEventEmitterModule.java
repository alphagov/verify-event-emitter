package uk.gov.ida.eventemitter.utils;

import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.gov.ida.eventemitter.Configuration;

import javax.annotation.Nullable;

public class TestEventEmitterModule extends AbstractModule {

    public TestEventEmitterModule() {
    }

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    @Nullable
    private AmazonSQS getAmazonSqs(final Configuration configuration) {
        if (configuration.isEnabled() && configuration.getSourceQueueName() != null) {
            return TestUtils.getClientSQS();
        }
        return null;
    }

    @Provides
    @Singleton
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }
}
