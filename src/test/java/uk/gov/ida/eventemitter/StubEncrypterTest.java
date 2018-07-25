package uk.gov.ida.eventemitter;

import org.junit.Test;
import uk.gov.ida.eventemitter.utils.TestEvent;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

public class StubEncrypterTest {

    @Test
    public void shouldReturnEncryptedEvent() {
        final StubEncrypter encrypter = new StubEncrypter();
        final TestEvent event = aTestEventMessage().build();

        final String actualValue = encrypter.encrypt(event);

        assertThat(actualValue).isEqualTo(String.format("Encrypted Event Id %s", event.getEventId().toString()));
    }
}
