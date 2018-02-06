package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;

import java.util.UUID;

public interface Event {

    UUID getEventId();

    DateTime getTimestamp();
}
