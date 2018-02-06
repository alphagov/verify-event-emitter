package uk.gov.ida.eventemitter;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventEmitterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private EventEmitter eventEmitter;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));

        eventEmitter = new EventEmitter();
    }

    @After
    public void tearDown() {
        System.setOut(System.out);
    }

    @Test
    public void recordMethodShouldWriteEventDetailsToSystemOut() {
        final UUID id = UUID.randomUUID();
        final String timestamp = "2018-02-06T14:37:55.467Z";
        final TestEvent event = new TestEvent(id, DateTime.parse(timestamp));

        eventEmitter.record(event);

        assertThat(outContent.toString())
            .isEqualTo(String.format("Event ID: %s, Timestamp: %s\n", event.getEventId(), timestamp));
    }

    @Test
    public void shouldNotThrowErrorsIfInputsAreNull() {
        final TestEvent event = new TestEvent(null, null);

        eventEmitter.record(event);

        assertThat(outContent.toString())
            .isEqualTo("Event ID: null, Timestamp: null\n");
    }
}
