package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.regions.Regions;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import org.junit.Test;
import uk.gov.ida.eventemitter.EventEmitterTestHelper;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatCode;

public class EventEmitterSQSModuleTest {

    @Test
    public void shouldThrowProvisionExceptionWhenQueueNameInvalid() {

        final EventEmitterSQS eventEmitterSQS;

        final Injector injector = EventEmitterSQSTestHelper.createTestConfiguration(
                Stage.DEVELOPMENT,
                true,
                "key",
                "secretkey",
                Regions.EU_WEST_2,
                null,
                null,
                ""
        );

        assertThatExceptionOfType(ProvisionException.class).isThrownBy(() -> {
            injector.getInstance(EventEmitterSQS.class);
        }).withMessageContaining("The security token included in the request is invalid");
    }

    @Test
    public void shouldNotThrowExceptionWhenDisabled() {

        final EventEmitterSQS eventEmitterSQS;

        final Injector injector = EventEmitterSQSTestHelper.createTestConfiguration(
                Stage.DEVELOPMENT,
                false,
                "key",
                "secretkey",
                Regions.EU_WEST_2,
                null,
                null,
                ""
        );

        assertThatCode(() -> {
            injector.getInstance(EventEmitterSQS.class);
        }).doesNotThrowAnyException();

    }

    @Test
    public void shouldThrowCreationExceptionWhenQueueNameInvalidProduction() {

        assertThatExceptionOfType(CreationException.class).isThrownBy(() -> {
            final Injector injector = EventEmitterSQSTestHelper.createTestConfiguration(
                    Stage.PRODUCTION,
                    true,
                    "key",
                    "secretkey",
                    Regions.EU_WEST_2,
                    null,
                    null,
                    ""
            );
        }).withMessageContaining("The security token included in the request is invalid");
    }

    @Test
    public void shouldNotCreateEventEmitterSQSWhenUsingEventEmitterModule() {
        final Injector injector = EventEmitterTestHelper.createTestConfiguration(
                true,
                "key",
                "secretkey",
                Regions.EU_WEST_2,
                null);
        assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> {
            injector.getInstance(EventEmitterSQS.class);
        }).withMessageContaining("No implementation for uk.gov.ida.eventemitter.sqs.SqsClient was bound");
    }
}
