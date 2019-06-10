package uk.gov.ida.eventemitter.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import junit.framework.Test;
import org.joda.time.DateTime;
import uk.gov.ida.eventemitter.AuditEvent;
import uk.gov.ida.eventemitter.Event;
import uk.gov.ida.eventemitter.EventDetailsKey;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TestEvent implements AuditEvent {

    @JsonProperty
    private String eventId;

    @JsonProperty
    private DateTime timestamp;

    private TestEvent() {}

    public TestEvent(final String eventId,
                     final DateTime timestamp) {
        this.eventId = eventId;
        this.timestamp = timestamp;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getLoggableMessage() {
        return "Loggable Message";
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestEvent testEvent = (TestEvent) o;

        return Objects.equals(eventId, testEvent.eventId) &&
            Objects.equals(timestamp, testEvent.timestamp) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp);
    }
}
