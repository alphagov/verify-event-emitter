package uk.gov.ida.eventemitter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;

import javax.inject.Named;

public class SqsClient {

    private final AmazonSQS sqs;
    private final String eventQueueUrl;

    @Inject
    public SqsClient(final AmazonSQS sqs, @Named("EventQueueUrl") final String eventQueueUrl) {
        this.sqs = sqs;
        this.eventQueueUrl = eventQueueUrl;
    }

    public void sendToSqs(String event) {
        try {
            final SendMessageRequest sendMessageRequest = new SendMessageRequest(eventQueueUrl, event);
            sqs.sendMessage(sendMessageRequest);
            System.out.println("Sent a message to the queue successfully.");
        } catch (AmazonClientException ace) {
            System.err.println("Failed to send the message to the queue. Error Message: " + ace.getMessage());
        }
    }
}
