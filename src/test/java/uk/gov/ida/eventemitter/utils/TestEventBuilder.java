package uk.gov.ida.eventemitter.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestEventBuilder {

    private UUID eventId = UUID.randomUUID();
    private DateTime timestamp = DateTime.now().withZone(DateTimeZone.UTC);
    private String eventType = "error";
    private Map<String, String> details= new HashMap<>();
    {
        details.put("message", "Session error");
        details.put("error_id", UUID.randomUUID().toString());
    }

    public static TestEventBuilder aTestEventMessage() {
        return new TestEventBuilder();
    }

    public TestEvent build() {
        return new TestEvent(
            eventId,
            timestamp,
            eventType,
            details);
    }
}
