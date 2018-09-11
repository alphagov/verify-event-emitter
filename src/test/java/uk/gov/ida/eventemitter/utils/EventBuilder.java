package uk.gov.ida.eventemitter.utils;

import uk.gov.ida.eventemitter.Event;
import uk.gov.ida.eventemitter.EventDetailsKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventBuilder {

    private String eventType = "error";
    private String originatingService = "originating service";
    private String sessionId = "session id";
    private Map<EventDetailsKey, String> details = new HashMap<>();

    {
        details.put(EventDetailsKey.message, "Session error");
        details.put(EventDetailsKey.error_id, UUID.randomUUID().toString());
    }

    public static EventBuilder anEventMessage() {
        return new EventBuilder();
    }

    public Event build() {
        return new Event(
            originatingService,
            sessionId,
            eventType,
            details);
    }
}
