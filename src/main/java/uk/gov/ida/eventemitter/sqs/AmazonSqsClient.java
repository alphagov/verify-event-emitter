package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eventemitter.AuditEvent;

import javax.inject.Named;

public class AmazonSqsClient implements SqsClient {

    private static final Logger LOG = LoggerFactory.getLogger(AmazonSqsClient.class);

    private final AmazonSQS sqs;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    @Inject
    public AmazonSqsClient(final AmazonSQS sqs,
                           @Named("QueueUrl") final String queueUrl,
                           final ObjectMapper objectMapper) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper.registerModule(new JodaModule());
    }

    @Override
    public void send(final AuditEvent event) throws AmazonClientException {
        final SendMessageRequest sendMessageRequest;
        try {
            sendMessageRequest = new SendMessageRequest(queueUrl, objectMapper.writeValueAsString(event));
            sqs.sendMessage(sendMessageRequest);
            LOG.info(String.format("Sent a message [Event Id: %s] to the queue successfully. [%s]", event.getEventId(), event.getLoggableMessage()));
        } catch (JsonProcessingException e) {
            LOG.error(String.format("Unable to convert [Event Id: %s] to json. Message not sent to the queue. [%s]", event.getEventId(), event.getLoggableMessage()));
        }
    }
}
