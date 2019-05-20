package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.AmazonClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eventemitter.AuditEvent;

import javax.inject.Inject;

public class EventEmitterSQS {

    private static final Logger LOG = LoggerFactory.getLogger(EventEmitterSQS.class);

    private final SqsClient sqsClient;

    @Inject
    public EventEmitterSQS(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void record(AuditEvent auditEvent) {
        if (auditEvent != null) {
            try {
                sqsClient.send(auditEvent);
            } catch (AmazonClientException e) {
                LOG.error(String.format("Failed to send a message [Event Id: %s] to the queue. Error Message: %s", auditEvent.getEventId(), e.getMessage()));
            }
        } else {
            LOG.error("Unable to send a message due to event containing null value.");
        }
    }
}
