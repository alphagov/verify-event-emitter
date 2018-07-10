package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.eventemitter.utils.TestDecrypter;
import uk.gov.ida.eventemitter.utils.TestEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

public class EventEncrypterTest {

    private static final byte[] KEY = "aesEncryptionKey".getBytes();

    private TestEvent event;
    private EventEncrypter eventEncrypter;
    private TestDecrypter<TestEvent> decrypter;

    @Before
    public void setUp() {
        event = aTestEventMessage().build();

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
