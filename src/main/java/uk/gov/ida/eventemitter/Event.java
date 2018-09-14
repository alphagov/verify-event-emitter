package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

public interface Event {

    UUID getEventId();

    DateTime getTimestamp();

    String getEventType();

    Map<EventDetailsKey,String> getDetails();

    String getOriginatingService();

    String getSessionId();
}
