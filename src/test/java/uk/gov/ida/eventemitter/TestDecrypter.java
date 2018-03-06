package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TestDecrypter<T> {

    private static final int IV_SIZE = 16;
    private final byte[] key;
    private final ObjectMapper mapper;

    @Inject
    public TestDecrypter(final byte[] key,
                         final ObjectMapper mapper) {
        this.key = key;
        this.mapper = mapper;
    }

    public T decrypt(final String bas64EncodedEncryptedEvent, final Class<T> klass) throws Exception {
        final byte[] encryptedEventAndIv = Base64.decodeBase64(bas64EncodedEncryptedEvent);
        final byte[] iv = extractIv(encryptedEventAndIv);
        final byte[] encryptedEvent = extractEncryptedEvent(encryptedEventAndIv);
        final byte[] event = decryptEncryptedEvent(encryptedEvent, iv);

        return (T) mapper.readValue(event, klass);
    }

    private byte[] extractIv(final byte[] encryptedEventAndIv) {
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encryptedEventAndIv, 0, iv, 0, iv.length);
        return  iv;
    }

    private byte[] extractEncryptedEvent(final byte[] encryptedEventAndIv) {
        int encryptedSize = encryptedEventAndIv.length - IV_SIZE;
        byte[] encryptedEvent = new byte[encryptedSize];
        System.arraycopy(encryptedEventAndIv, IV_SIZE, encryptedEvent, 0, encryptedSize);
        return encryptedEvent;
    }

    private byte[] decryptEncryptedEvent(final byte[] encryptedEvent, final byte[] iv) throws Exception {
        final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedEvent);
    }
}
