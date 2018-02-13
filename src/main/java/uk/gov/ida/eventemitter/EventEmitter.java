package uk.gov.ida.eventemitter;

import javax.inject.Inject;

public class EventEmitter {

    private final boolean sendToRecordingSystem;

    @Inject
    public EventEmitter(boolean sendToRecordingSystem) {
        this.sendToRecordingSystem = sendToRecordingSystem;
    }

    public void record(Event event) {
        if (sendToRecordingSystem) {
            System.out.println(
                String.format("Event ID: %s, Timestamp: %s, Event Type: %s",
                    event.getEventId(), event.getTimestamp(), event.getEventType()));
        }
    }
}
