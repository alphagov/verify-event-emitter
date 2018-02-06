package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;

import java.util.UUID;

public class TestEvent implements Event {

    private final UUID eventId;
    private final DateTime timestamp;

    public TestEvent(UUID eventId, DateTime timestamp) {
        this.eventId = eventId;
        this.timestamp = timestamp;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }
}
