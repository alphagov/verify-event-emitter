package uk.gov.ida.eventemitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

public class EventEmitter {

    private static final Logger LOG = LoggerFactory.getLogger(EventEmitter.class);

    private final Encrypter encrypter;
    private final EventSender eventSender;

    @Inject
    public EventEmitter(final Encrypter encrypter,
                        final EventSender eventSender) {
        this.encrypter = encrypter;
        this.eventSender = eventSender;
    }

    public void record(final Event event) {
        if (event != null) {
            String encryptedEvent = null;
            try {
                encryptedEvent = encrypter.encrypt(event);
                eventSender.sendAuthenticated(event, encryptedEvent);
                LOG.info(String.format("Sent Event Message [Event Id: %s]", event.getEventId()));
            } catch (AwsResponseException awsEx) {
                LOG.error(String.format("Failed to send a message [Event Id: %s] to the queue. Status: %s Error Message: %s", event.getEventId(), awsEx.getResponse().getStatusCode(), awsEx.getMessage()));
                LOG.error(String.format("Event Message: %s", encryptedEvent));
            } catch ( EventEncryptionException | java.io.UnsupportedEncodingException ex) {
                LOG.error(String.format("Failed to send a message [Event Id: %s] to the queue. Error Message: %s", event.getEventId(), ex.getMessage()));
                LOG.error(String.format("Event Message: %s", encryptedEvent));
            }
        } else {
            LOG.error("Unable to send a message due to event containing null value.");
        }
    }
}
