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
import javax.annotation.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.ByteBuffer;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    @Nullable
    private AWSCredentials getAmazonCredential(final Configuration configuration) {
        if (configuration.isEnabled()) {
            return new BasicAWSCredentials(configuration.getAccessKeyId(), configuration.getSecretAccessKey());
        }
        return null;
    }

    @Provides
    @Singleton
    @Nullable
    private AmazonSQS getAmazonSqs(
            final Configuration configuration,
            final AWSCredentials credentials) {
        if (configuration.isEnabled()) {
            return AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(configuration.getRegion())
                    .build();
        }
        return null;
    }

    @Provides
    @Singleton
    @Nullable
    private AmazonS3 getAmazonS3(
            final Configuration configuration,
            final AWSCredentials credentials) {
        if (configuration.isEnabled()) {
            return AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(configuration.getRegion())
                    .build();
        }
        return null;
    }

    @Provides
    @Singleton
    @Nullable
    private AWSKMS getAmazonKms(
            @Nullable final AmazonS3 amazonS3,
            final AWSCredentials credentials,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            return AWSKMSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(configuration.getRegion())
                    .build();
        }
        return null;
    }

    @Provides
    @Singleton
    @Nullable
    @Named("SourceQueueUrl")
    private String getQueueUrl(
            @Nullable final AmazonSQS amazonSqs,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            return amazonSqs.getQueueUrl(configuration.getSourceQueueName()).getQueueUrl();
        }
        return null;
    }

    @Provides
    @Singleton
    private SqsClient getAmazonSqsClient(
            @Nullable final AmazonSQS amazonSqs,
            @Nullable final @Named("SourceQueueUrl") String sourceQueueUrl,
            final Configuration configuration) {
        if (configuration.isEnabled()) {
            return new AmazonSqsClient(amazonSqs, sourceQueueUrl);
        }
        return new StubSqsClient();
    }

    @Provides
    @Singleton
    private Encrypter getEncrypter(
            @Nullable final AmazonS3 amazonS3,
            final Configuration configuration,
            final ObjectMapper mapper,
            final AWSKMS amazonKms) {
        if (configuration.isEnabled()) {
            try {
                S3Object s3Object = amazonS3.getObject(configuration.getBucketName(), configuration.getKeyName());
                try (S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent()) {
                    DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(Base64.decode(IOUtils.toString(s3ObjectInputStream))));
                    DecryptResult key = amazonKms.decrypt(request);

                    return new EventEncrypter(key.getPlaintext().array(), mapper);
                }
            } catch (SdkClientException e) {
                throw new EventEmitterConfigurationException(
                    String.format("Failed to load S3 bucket %s.", configuration.getBucketName())
                );
            } catch (IOException e) {
                throw new EventEmitterConfigurationException(
                    String.format("Failed to read data from %s in %s.", configuration.getKeyName(), configuration.getBucketName())
                );
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
}
