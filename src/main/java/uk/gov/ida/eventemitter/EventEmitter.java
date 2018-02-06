package uk.gov.ida.eventemitter;

public class EventEmitter {

    public void record(Event event) {
        System.out.println(String.format("Event ID: %s, Timestamp: %s", event.getEventId(), event.getTimestamp()));
    }
}
