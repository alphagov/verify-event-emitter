package uk.gov.ida.eventemitter;

import javax.inject.Inject;

public class EventEmitter {

    private final Encrypter encrypter;
    private final SqsClient sqsClient;

    @Inject
    public EventEmitter(final Encrypter encrypter,
                        final SqsClient sqsClient) {
        this.encrypter = encrypter;
        this.sqsClient = sqsClient;
    }

    public void record(final Event event) {
        if (event != null) {
            String encryptedEvent = null;
            try {
                encryptedEvent = encrypter.encrypt(event);
                sqsClient.send(event, encryptedEvent);
            } catch (Exception e) {
                System.err.println(String.format("Failed to send a message [Event Id: %s] to the queue. Error Message: %s", event.getEventId(), e.getMessage()));
                System.err.println(String.format("Event Message: %s", encryptedEvent));
            }
        } else {
            System.err.println("Unable to send a message due to event containing null value.");
        }
    }
}
