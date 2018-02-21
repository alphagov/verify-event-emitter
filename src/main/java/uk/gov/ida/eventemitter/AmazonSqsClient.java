package uk.gov.ida.eventemitter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;

import javax.inject.Named;

public class AmazonSqsClient implements SqsClient {

    private final AmazonSQS sqs;
    private final String queueUrl;

    @Inject
    public AmazonSqsClient(final AmazonSQS sqs,
                           @Named("QueueUrl") final String queueUrl) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
    }

    @Override
    public void send(final Event event, final String encryptedEvent) throws AmazonClientException {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, encryptedEvent);
        sqs.sendMessage(sendMessageRequest);
        System.out.println(String.format("Sent a message [Event Id: %s] to the queue successfully.", event.getEventId()));
    }
}
