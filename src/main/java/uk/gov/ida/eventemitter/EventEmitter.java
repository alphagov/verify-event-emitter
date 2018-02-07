package uk.gov.ida.eventemitter;

import javax.inject.Inject;

public class EventEmitter {

    @Inject
    public EventEmitter() {}

    public void record(Event event) {
        System.out.println(String.format("Event ID: %s, Timestamp: %s", event.getEventId(), event.getTimestamp()));
    }
}
