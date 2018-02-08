package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;

import java.util.UUID;

public class TestEvent implements Event {

    private final UUID eventId;
    private final DateTime timestamp;
    private final String eventType;

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
}
