package uk.gov.ida.eventemitter;

import javax.inject.Inject;

public class EventEmitter {

    private final boolean sendToRecordingSystem;
    private final Encrypter encrypter;
    private final SqsClient sqsClient;

    @Inject
    public EventEmitter(final boolean sendToRecordingSystem,
                        final Encrypter encrypter,
                        final SqsClient sqsClient) {
        this.sendToRecordingSystem = sendToRecordingSystem;
        this.encrypter = encrypter;
        this.sqsClient = sqsClient;
    }

    public void record(final Event event) {
        if (sendToRecordingSystem) {
            String encryptedEvent = null;
            try {
                encryptedEvent = encrypter.encrypt(event);
                System.out.println(String.format(
                    "Event ID: %s, Timestamp: %s, Event Type: %s, Event String: %s",
                    event.getEventId(),
                    event.getTimestamp(),
                    event.getEventType(),
                    encryptedEvent
                ));
                sqsClient.sendToSqs(encryptedEvent);
            } catch (Exception e) {
                System.err.println(String.format("Failed to record an event %s. Error message: %s", event.getEventId(), e.getMessage()));
                System.err.println(String.format("Event Message: %s", encryptedEvent));
            }
        }
    }
}
