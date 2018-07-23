package uk.gov.ida.eventemitter.utils;

import cloud.localstack.TestUtils;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.gov.ida.eventemitter.Configuration;

public class TestEventEmitterModule extends AbstractModule {

    private final AWSKMS awsKms;

    public TestEventEmitterModule(AWSKMS awsKms) {
        this.awsKms = awsKms;
    }

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    private AmazonSQS getAmazonSqs(final Configuration configuration) {
        if (configuration.isEnabled() && configuration.getSourceQueueName() != null) {
            return TestUtils.getClientSQS();
        }
        return null;
    }

    @Provides
    @Singleton
    private AmazonS3 getAmazonS3(final Configuration configuration) {
        if (configuration.isEnabled() &&
            configuration.getBucketName() != null &&
            configuration.getKeyName() != null) {
            return TestUtils.getClientS3();
        }
        return null;
    }

    @Provides
    @Singleton
    private AWSKMS getAmazonKms(AmazonS3 amazonS3) {
        return awsKms;
    }

    @Provides
    @Singleton
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }
}
