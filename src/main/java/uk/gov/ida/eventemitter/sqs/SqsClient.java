package uk.gov.ida.eventemitter.sqs;

import uk.gov.ida.eventemitter.AuditEvent;

public interface SqsClient {

    void send(final AuditEvent event);
}
