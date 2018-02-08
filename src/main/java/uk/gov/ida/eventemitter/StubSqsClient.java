package uk.gov.ida.eventemitter;

public class StubSqsClient implements SqsClient {

    @Override
    public void send(final Event event, final String encryptedEvent) {
        System.out.println(String.format(
            "Event ID: %s, Timestamp: %s, Event Type: %s, Event String: %s",
            event.getEventId(),
            event.getTimestamp(),
            event.getEventType(),
            encryptedEvent
        ));
    }
}
