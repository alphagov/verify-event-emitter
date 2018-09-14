package uk.gov.ida.eventemitter.utils;

import uk.gov.ida.eventemitter.Event;
import uk.gov.ida.eventemitter.EventDetailsKey;
import uk.gov.ida.eventemitter.EventMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventMessageBuilder {

    private String eventType = "error";
    private String originatingService = "originating service";
    private String sessionId = "session id";
    private Map<EventDetailsKey, String> details = new HashMap<>();

    {
        details.put(EventDetailsKey.message, "Session error");
        details.put(EventDetailsKey.error_id, UUID.randomUUID().toString());
    }

    public static EventMessageBuilder anEventMessage() {
        return new EventMessageBuilder();
    }

    public EventMessageBuilder withDetailsField(final EventDetailsKey key, final String value) {
        details.put(key, value);
        return this;
    }

    public Event build() {
        return new EventMessage(
            originatingService,
            sessionId,
            eventType,
            details);
    }
}
