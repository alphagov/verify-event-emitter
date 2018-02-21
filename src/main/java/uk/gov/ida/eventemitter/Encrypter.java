package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Encrypter {

    String encrypt(final Event event) throws JsonProcessingException;
}
