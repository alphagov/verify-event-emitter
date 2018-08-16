package uk.gov.ida.eventemitter;

import com.amazonaws.Response;

public interface EventSender {

    Response<Void> sendAuthenticated(final Event event, final String encryptedEvent) throws java.io.UnsupportedEncodingException;
}
