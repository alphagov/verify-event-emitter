package uk.gov.ida.eventemitter.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.UUID;

public class TestEventBuilder {

    private String eventId = UUID.randomUUID().toString();
    private DateTime timestamp = DateTime.now().withZone(DateTimeZone.UTC);
    private TestEvent testEvent = new TestEvent(eventId, timestamp);

    public static TestEventBuilder newInstance() {
        return new TestEventBuilder();
    }

    public TestEventBuilder withEventId(String eventId) {
        testEvent.setEventId(eventId);
        return this;
    }

    public TestEventBuilder withTimestamp(DateTime timestamp) {
        testEvent.setTimestamp(timestamp);
        return this;
    }

    public TestEvent build() {
        return testEvent;
    }
}
