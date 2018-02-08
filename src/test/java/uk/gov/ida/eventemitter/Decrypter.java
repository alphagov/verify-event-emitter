package uk.gov.ida.eventemitter;

public interface Decrypter<T> {

    T decrypt(final String encryptedEvent, final Class<T> klass) throws Exception;
}
