package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.Objects;
import java.util.UUID;

public final class TestEvent implements Event {

    @JsonProperty
    private UUID eventId;

    @JsonProperty
    private DateTime timestamp;

    @JsonProperty
    private String eventType;

    private TestEvent() {}

    public TestEvent(final UUID eventId, final DateTime timestamp, final String eventType) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestEvent{");
        sb.append("eventId=").append(eventId);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", eventType='").append(eventType).append('\'');
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
            Objects.equals(eventType, testEvent.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, eventType);
    }
}
