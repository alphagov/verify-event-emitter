package uk.gov.ida.eventemitter.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import uk.gov.ida.eventemitter.Event;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class TestEvent implements Event {

    @JsonProperty
    private UUID eventId;

    @JsonProperty
    private DateTime timestamp;

    @JsonProperty
    private String eventType;

    @JsonProperty
    private Map<String, String> details;

    private TestEvent() {
    }

    public TestEvent(final UUID eventId,
                     final DateTime timestamp,
                     final String eventType,
                     final Map<String, String> details) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.details = details;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestEvent{");
        sb.append("eventId=").append(eventId);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", eventType='").append(eventType).append('\'');
        sb.append(", details=").append(details);
        sb.append('}');
        return sb.toString();
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
                Objects.equals(timestamp, testEvent.timestamp) &&
                Objects.equals(eventType, testEvent.eventType) &&
                Objects.equals(details, testEvent.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, eventType, details);
    }
}
