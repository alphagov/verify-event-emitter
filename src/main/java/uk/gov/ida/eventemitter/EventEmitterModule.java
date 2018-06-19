package uk.gov.ida.eventemitter;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
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
    private Optional<AWSCredentials> getAmazonCredential(final Optional<Configuration> configuration) {
        return configuration.map(
            config -> {
                if (config.getAccessKeyId() != null && config.getSecretAccessKey() != null) {
                    return new BasicAWSCredentials(config.getAccessKeyId(), config.getSecretAccessKey());
                }
                return null;
            });
    }

    @Provides
    @Singleton
    private Optional<AmazonSQS> getAmazonSqs(
        final Optional<Configuration> configuration,
        final Optional<AWSCredentials> credentials) {

        return configuration.map(
            config -> {
                if (config.getSourceQueueName() != null) {
                    if (hasAmazonCredentialWithARegion(credentials, configuration)) {
                        return AmazonSQSClientBuilder.standard()
                                                     .withCredentials(new AWSStaticCredentialsProvider(credentials.get()))
                                                     .withRegion(config.getRegion())
                                                     .build();
                    }
                    return AmazonSQSClientBuilder.defaultClient();
                }
                return null;
            });
    }

    @Provides
    @Singleton
    private Optional<AmazonS3> getAmazonS3(
        final Optional<Configuration> configuration,
        final Optional<AWSCredentials> credentials) {

        return configuration.map(
            config -> {
                if (config.getBucketName() != null && config.getKeyName() != null) {
                    if (hasAmazonCredentialWithARegion(credentials, configuration)) {
                        return AmazonS3ClientBuilder.standard()
                                                    .withCredentials(new AWSStaticCredentialsProvider(credentials.get()))
                                                    .withRegion(config.getRegion())
                                                    .build();
                    }
                    AmazonS3ClientBuilder.defaultClient();
                }
                return null;
            });
    }

    @Provides
    @Singleton
    private Optional<AWSKMS> getAmazonKms(
        final Optional<AmazonS3> amazonS3,
        final Optional<AWSCredentials> credentials,
        final Optional<Configuration> configuration) {

        return amazonS3.map(s3 -> {
            if (hasAmazonCredentialWithARegion(credentials, configuration)) {
                return AWSKMSClientBuilder.standard()
                                          .withCredentials(new AWSStaticCredentialsProvider(credentials.get()))
                                          .withRegion(configuration.get().getRegion())
                                          .build();
            }
            return AWSKMSClientBuilder.defaultClient();
        });
    }

    @Provides
    @Singleton
    @Named("SourceQueueUrl")
    private Optional<String> getQueueUrl(
        final Optional<AmazonSQS> amazonSqs,
        final Optional<Configuration> configuration) {

        return amazonSqs.map(sqs -> sqs.getQueueUrl(configuration.get().getSourceQueueName()).getQueueUrl());
    }

    @Provides
    @Singleton
    private SqsClient getAmazonSqsClient(
        final Optional<AmazonSQS> amazonSqs,
        final @Named("SourceQueueUrl") Optional<String> sourceQueueUrl) {

        if (amazonSqs.isPresent() && sourceQueueUrl.isPresent()){
            return new AmazonSqsClient(amazonSqs.get(), sourceQueueUrl.get());
        }
        return new StubSqsClient();
    }

    @Provides
    @Singleton
    private Encrypter getEncrypter(
        final Optional<AmazonS3> amazonS3,
        final Optional<Configuration> configuration,
        final ObjectMapper mapper,
        final Optional<AWSKMS> amazonKms) {

        if (amazonS3.isPresent() && amazonKms.isPresent()) {
            try {
                S3Object s3Object = amazonS3.get().getObject(configuration.get().getBucketName(), configuration.get().getKeyName());
                try (S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent()) {
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
    private EventEmitter getEventEmitter(
        final SqsClient sqsClient,
        final Encrypter encrypter) {

        return new EventEmitter(encrypter, sqsClient);
    }

    private boolean hasAmazonCredentialWithARegion(
        final Optional<AWSCredentials> credentials,
        final Optional<Configuration> configuration) {

        return credentials.isPresent() && configuration.isPresent() && configuration.get().getRegion() != null;
    }
}
