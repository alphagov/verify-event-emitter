package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.eventemitter.utils.TestDecrypter;
import uk.gov.ida.eventemitter.utils.TestEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventEncrypterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";
    private static final byte[] KEY = "aesEncryptionKey".getBytes();

    private TestEvent event;
    private EventEncrypter eventEncrypter;
    private TestDecrypter<TestEvent> decrypter;

    @Before
    public void setUp() {
        final Map<String, String> details = new HashMap<>();
        details.put("type", "network error");
        event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE, details);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());

        eventEncrypter = new EventEncrypter(KEY, mapper);
        decrypter = new TestDecrypter<>(KEY, mapper);
    }

    @Test
    public void shouldEncryptEvent() throws Exception {
        final String encryptedEvent = eventEncrypter.encrypt(event);

        TestEvent actualEvent = decrypter.decrypt(encryptedEvent, TestEvent.class);

        assertThat(actualEvent).isEqualTo(event);
    }
}
