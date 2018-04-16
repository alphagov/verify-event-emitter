package uk.gov.ida.eventemitter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    private Optional<AmazonSQS> getAmazonSqs(final Optional<Configuration> configuration) {
        if (configuration.isPresent() &&
            configuration.get().getSourceQueueName() != null) {
            return Optional.ofNullable(AmazonSQSClientBuilder.defaultClient());
        }
        return Optional.empty();
    }

    @Provides
    private Optional<AmazonS3> getAmazonS3(final Optional<Configuration> configuration) {
        if (configuration.isPresent() &&
            configuration.get().getBucketName() != null &&
            configuration.get().getKeyName() != null) {
            return Optional.ofNullable(AmazonS3ClientBuilder.defaultClient());
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
    private Encrypter getEncrypter(final Optional<AmazonS3> amazonS3,
                                   final Optional<Configuration> configuration,
                                   final ObjectMapper mapper) {
        if (amazonS3.isPresent()) {
            try {
                S3Object s3Object = amazonS3.get().getObject(configuration.get().getBucketName(), configuration.get().getKeyName());
                S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
                byte[] key = IOUtils.toByteArray(s3ObjectInputStream);
                return new EventEncrypter(key, mapper);

            } catch (SdkClientException e) {
                System.err.println(
                    String.format("Failed to load S3 bucket %s.", configuration.get().getBucketName()));
            }
            catch (IOException e) {
                System.err.println(
                    String.format("Failed to read data from %s in %s.", configuration.get().getKeyName(), configuration.get().getBucketName()));
            }
        }
        return new StubEncrypter();
    }

    @Provides
    private EventEmitter getEventEmitter(final SqsClient sqsClient,
                                         final Encrypter encrypter) {
        return new EventEmitter(encrypter, sqsClient);
    }
}
