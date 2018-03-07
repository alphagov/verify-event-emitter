package uk.gov.ida.eventemitter;

import cloud.localstack.TestUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.util.Optional;

public class TestEventEmitterModule extends AbstractModule {
    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs(final Optional<Configuration> configuration) {
        if (configuration.isPresent() && configuration.get().getSourceQueueName() != null) {
            return Optional.ofNullable(TestUtils.getClientSQS());
        }
        return Optional.empty();
    }

    @Provides
    private Optional<AmazonS3> getAmazonS3(final Optional<Configuration> configuration) {
        if (configuration.isPresent() &&
            configuration.get().getBucketName() != null &&
            configuration.get().getKeyName() != null) {
            return Optional.ofNullable(TestUtils.getClientS3());
        }
        return Optional.empty();
    }

    @Provides
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }
}
