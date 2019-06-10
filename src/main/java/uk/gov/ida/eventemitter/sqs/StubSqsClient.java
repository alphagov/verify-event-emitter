package uk.gov.ida.eventemitter.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eventemitter.AuditEvent;

public class StubSqsClient implements SqsClient {

    private static final Logger LOG = LoggerFactory.getLogger(StubSqsClient.class);

    @Override
    public void send(final AuditEvent auditEvent) {

       LOG.debug(String.format(
                "Event ID: %s, Timestamp: %s. [%s]",
                auditEvent.getEventId(),
                auditEvent.getTimestamp(),
                auditEvent.getLoggableMessage()
        ));

    }
}
