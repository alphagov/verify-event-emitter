package uk.gov.ida.eventemitter;

import cloud.localstack.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.util.Optional;

public class TestEventEmitterModule extends AbstractModule {

    public static final String KEY = "aesEncryptionKey";
    public static final String INIT_VECTOR = "encryptionIntVec";

    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs(final Optional<Configuration> configuration) {
        if (configuration.isPresent() && configuration.get().getQueueName() != null) {
            return Optional.ofNullable(TestUtils.getClientSQS());
        }
        return Optional.empty();
    }

    @Provides
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }

    @Provides
    private EventEmitter getEventEmitter(final SqsClient sqsClient, final ObjectMapper mapper) {

        return new EventEmitter(new TestEncrypter(KEY, INIT_VECTOR, mapper), sqsClient);
    }
}
