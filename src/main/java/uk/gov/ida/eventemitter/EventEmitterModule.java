package uk.gov.ida.eventemitter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    private Optional<AmazonSQS> getAmazonSqs(final Optional<Configuration> configuration) {
        if (configuration.isPresent() &&
            configuration.get().getSourceQueueName() != null) {
            return Optional.ofNullable(AmazonSQSClientBuilder.defaultClient());
        }
        return Optional.empty();
    }

    @Provides
    @Singleton
    private Optional<AmazonS3> getAmazonS3(final Optional<Configuration> configuration) {
        if (configuration.isPresent() &&
            configuration.get().getBucketName() != null &&
            configuration.get().getKeyName() != null) {
            return Optional.ofNullable(AmazonS3ClientBuilder.defaultClient());
        }
        return Optional.empty();
    }

    @Provides
    @Singleton
    @Named("SourceQueueUrl")
    private Optional<String> getQueueUrl(final Optional<AmazonSQS> amazonSqs,
                                         final Optional<Configuration> configuration) {
        return amazonSqs.map(sqs -> sqs.getQueueUrl(configuration.get().getSourceQueueName()).getQueueUrl());
    }

    @Provides
    @Singleton
    private SqsClient getAmazonSqsClient(final Optional<AmazonSQS> amazonSqs,
                                         final @Named("SourceQueueUrl") Optional<String> sourceQueueUrl) {
        if (amazonSqs.isPresent() && sourceQueueUrl.isPresent()){
            return new AmazonSqsClient(amazonSqs.get(), sourceQueueUrl.get());
        }
        return new StubSqsClient();
    }

    @Provides
    @Singleton
    private Optional<AWSKMS> getAmazonKms(Optional<AmazonS3> amazonS3) {
        return amazonS3.map(s3 -> AWSKMSClientBuilder.defaultClient());
    }

    @Provides
    @Singleton
    private Encrypter getEncrypter(final Optional<AmazonS3> amazonS3,
                                   final Optional<Configuration> configuration,
                                   final ObjectMapper mapper,
                                   final Optional<AWSKMS> amazonKms) {
        if (amazonS3.isPresent() && amazonKms.isPresent()) {
            try {
                S3Object s3Object = amazonS3.get().getObject(configuration.get().getBucketName(), configuration.get().getKeyName());
                try (S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();) {
                    DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(Base64.decode(IOUtils.toString(s3ObjectInputStream))));
                    DecryptResult key = amazonKms.get().decrypt(request);

                    return new EventEncrypter(key.getPlaintext().array(), mapper);
                }
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
    @Singleton
    private EventEmitter getEventEmitter(final SqsClient sqsClient,
                                         final Encrypter encrypter) {
        return new EventEmitter(encrypter, sqsClient);
    }
}
