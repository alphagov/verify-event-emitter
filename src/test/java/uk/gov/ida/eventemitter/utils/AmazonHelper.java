package uk.gov.ida.eventemitter.utils;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.List;

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

    public static String getQueueUrl(final Injector injector) {
        return injector.getInstance(
            Key.get(TypeLiteral.get(String.class), Names.named("SourceQueueUrl")));
    }

    public static AmazonSQS getInstanceOfAmazonSqs(final Injector injector) {
        return injector.getInstance(
                Key.get(TypeLiteral.get(AmazonSQS.class)));
    }
}
