package uk.gov.ida.eventemitter.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class AmazonHelper {

    private AmazonHelper() {}

    public static Message getAMessageFromSqs(final AmazonSQS sqs,
                                             final String queueUrl) {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

        return messages.get(0);
    }

    public static void createSourceQueue(final AmazonSQS sqs,
                                         final String sourceQueueName) {
        try {
            sqs.createQueue(sourceQueueName);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
    }

    public static void setUpS3Bucket(final AmazonS3 s3,
                                     final String bucketName,
                                     final String keyName,
                                     final String key) {
        try {
            s3.createBucket(bucketName);
        } catch (AmazonS3Exception e) {
            System.err.println(e.getErrorMessage());
        }

        try {
            s3.putObject(bucketName, keyName, key);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    public static void deleteBucket(final AmazonS3 s3,
                                    final String bucketName) {
        try {
            ObjectListing objectListing = s3.listObjects(bucketName);
            while (true) {
                for (Iterator<?> iterator =
                     objectListing.getObjectSummaries().iterator();
                     iterator.hasNext();) {
                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                    s3.deleteObject(bucketName, summary.getKey());
                }

                if (objectListing.isTruncated()) {
                    objectListing = s3.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            };
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    public static Optional<String> getQueueUrl(final Injector injector) {
        return (Optional<String>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, String.class)), Names.named("SourceQueueUrl")));
    }

    public static Optional<AmazonSQS> getInstanceOfAmazonSqs(final Injector injector) {
        return (Optional<AmazonSQS>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, AmazonSQS.class))));
    }

    public static Optional<AmazonS3> getInstanceOfAmazonS3(final Injector injector) {
        return (Optional<AmazonS3>) injector.getInstance(
            Key.get(TypeLiteral.get(Types.newParameterizedType(Optional.class, AmazonS3.class))));
    }
}
