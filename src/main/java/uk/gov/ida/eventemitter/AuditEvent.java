package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;

public interface AuditEvent {
    public String getEventId();
    public DateTime getTimestamp();
    public String getLoggableMessage();
}
