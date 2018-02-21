package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StubEncrypterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final DateTime TIMESTAMP = DateTime.now(DateTimeZone.UTC);
    private static final String EVENT_TYPE = "Error Event";

    @Test
    public void shouldReturnEncryptedEvent() {
        final StubEncrypter encrypter = new StubEncrypter();
        final TestEvent event = new TestEvent(ID, TIMESTAMP, EVENT_TYPE);

        final String actualValue = encrypter.encrypt(event);

        assertThat(actualValue).isEqualTo(String.format("Encrypted Event Id %s", ID));
    }
}
