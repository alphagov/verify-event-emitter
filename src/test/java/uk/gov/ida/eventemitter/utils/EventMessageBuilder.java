package uk.gov.ida.eventemitter.utils;

import uk.gov.ida.eventemitter.EventDetailsKey;
import uk.gov.ida.eventemitter.EventMessage;

import java.util.EnumMap;
import java.util.UUID;

public class EventMessageBuilder {

    private String eventType = "error";
    private String originatingService = "originating service";
    private String sessionId = "session id";
    private EnumMap<EventDetailsKey, String> details = new EnumMap<>(EventDetailsKey.class);

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

    public EventMessage build() {
        return new EventMessage(
            originatingService,
            sessionId,
            eventType,
            details);
    }
}
