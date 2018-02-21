package uk.gov.ida.eventemitter;

public interface SqsClient {

    void send(final Event event, final String encryptedEvent);
}
