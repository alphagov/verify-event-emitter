package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TestDecrypter<T> {

    private final String key;
    private final String initVector;
    private final ObjectMapper mapper;

    @Inject
    public TestDecrypter(final String key,
                         final String initVector,
                         final ObjectMapper mapper) {
        this.key = key;
        this.initVector = initVector;
        this.mapper = mapper;
    }

    public T decrypt(final String encryptedEvent, final Class<T> klass) throws Exception {
        final IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        final SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        final byte[] original = cipher.doFinal(Base64.decodeBase64(encryptedEvent));
        return (T) mapper.readValue(original, klass);
    }
}
