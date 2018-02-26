package uk.gov.ida.eventemitter;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonEncrypterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";
    private static final String ENCRYPTED_EVENT = "encryptedEvent";
    private static final String JSON_STRING = "JSONString";

    @Mock
    private AwsCrypto awsCrypto;

    @Mock
    private KmsMasterKeyProvider provider;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CryptoResult cryptoResult;


    @Test
    public void shouldEncryptEvent() throws JsonProcessingException {
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        final TestEvent event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE, details);
        final AmazonEncrypter encrypter = new AmazonEncrypter(awsCrypto, provider, mapper);
        final Map<String, String> context = Collections.EMPTY_MAP;

        when(mapper.writeValueAsString(event)).thenReturn(JSON_STRING);
        when(awsCrypto.encryptString(provider, JSON_STRING, context)).thenReturn(cryptoResult);
        when(cryptoResult.getResult()).thenReturn(ENCRYPTED_EVENT);

        final String actualValue = encrypter.encrypt(event);

        assertThat(actualValue).isEqualTo(ENCRYPTED_EVENT);
    }
}
