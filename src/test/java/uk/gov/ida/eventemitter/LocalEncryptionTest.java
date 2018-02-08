package uk.gov.ida.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocalEncryptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String KEY = "aesEncryptionKey";
    private static final String INIT_VEC = "encryptionIntVec";
    private Encrypter encrypter;
    private Decrypter<TestEvent> decrypter;
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Event EVENT = new TestEvent(EVENT_ID, DateTime.now(DateTimeZone.UTC), "eventType");
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        mapper.registerModule(new JodaModule());
        encrypter = new LocalEncrypter(KEY, INIT_VEC, mapper);
        decrypter = new LocalDecrypter(KEY, INIT_VEC, mapper);
    }

    @Test
    public void shouldEncrypt() throws Exception {
        final String encryptedEvent = encrypter.encrypt(EVENT);
        final TestEvent actualEvent = decrypter.decrypt(encryptedEvent, TestEvent.class);

        assertThat(actualEvent).isEqualToComparingFieldByFieldRecursively(EVENT);
    }

    @Test
    public void ShouldLogErrorAfterFailingToEncrypt() throws Exception {
        expectedException.expect(InvalidKeyException.class);

        final Encrypter brokenEncrypter = new LocalEncrypter("badKey", INIT_VEC, mapper);

        try (ByteArrayOutputStream errContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(errContent )) {
            System.setErr(printStream);
            final String encryptedEvent = brokenEncrypter.encrypt(EVENT);
            System.setErr(System.err);
        }
    }

    @Test
    public void ShouldLogErrorAfterFailingToDecrypt() throws Exception {
        expectedException.expect(InvalidAlgorithmParameterException.class);

        final Decrypter<TestEvent> brokenDecrypter = new LocalDecrypter(KEY, "badInitVector", mapper);

        try (ByteArrayOutputStream errContent = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(errContent )) {
            System.setErr(printStream);
            final String encryptedEvent = encrypter.encrypt(EVENT);
            final Event actualEvent = brokenDecrypter.decrypt(encryptedEvent, TestEvent.class);
            System.setErr(System.err);

            assertThat(errContent.toString()).isEqualTo(String.format("Failed to decrypt an encrypted event. Error message: Wrong IV length: must be 16 bytes long\n", EVENT_ID));
            assertThat(actualEvent).isNull();
        }
    }
}
