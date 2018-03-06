package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class EventEncrypter implements Encrypter {

    private static final int IV_SIZE = 16;
    private final byte[] key;
    private final ObjectMapper mapper;

    @Inject
    public EventEncrypter(final byte[] key,
                          final ObjectMapper mapper) {
        this.key = key;
        this.mapper = mapper;
    }

    public String encrypt(final Event event) throws Exception {
        final byte[] iv = generateIV();
        final byte[] encryptedEvent = encryptEvent(event, iv);
        final byte[] encryptedEventAndIv = combineEncryptedEventWithIV(encryptedEvent, iv);

        return Base64.encodeBase64String(encryptedEventAndIv);
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private byte[] encryptEvent(final Event event, final byte[] iv) throws Exception {
        final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(mapper.writeValueAsBytes(event));
    }

    private byte[] combineEncryptedEventWithIV(final byte[] encryptedEvent, final byte[] iv) {
        byte[] encryptedEventAndIv = new byte[IV_SIZE + encryptedEvent.length];
        System.arraycopy(iv, 0, encryptedEventAndIv, 0, IV_SIZE);
        System.arraycopy(encryptedEvent, 0, encryptedEventAndIv, IV_SIZE, encryptedEvent.length);
        return encryptedEventAndIv;
    }
}
