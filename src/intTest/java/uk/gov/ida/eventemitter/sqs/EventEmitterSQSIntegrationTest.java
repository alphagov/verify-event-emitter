package uk.gov.ida.eventemitter.sqs;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ida.eventemitter.AuditEvent;

import static uk.gov.ida.eventemitter.sqs.EventEmitterSQSTestHelper.ACCESS_KEY_ID;
import static uk.gov.ida.eventemitter.sqs.EventEmitterSQSTestHelper.ACCESS_SECRET_KEY;
import static uk.gov.ida.eventemitter.sqs.EventEmitterSQSTestHelper.SOURCE_QUEUE_NAME;


public class EventEmitterSQSIntegrationTest {

    private static final boolean CONFIGURATION_ENABLED = true;

    private static Injector injector = null;
    private static EventEmitterSQS eventEmitterSQS;
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() {

        /*
        Will only run if the AWS Credentials are configured in Environment Variables.
         */
        org.junit.Assume.assumeTrue(ACCESS_KEY_ID != null && ACCESS_SECRET_KEY != null);

        injector = EventEmitterSQSTestHelper.createTestConfiguration(
                Stage.PRODUCTION,
                CONFIGURATION_ENABLED,
                ACCESS_KEY_ID,
                ACCESS_SECRET_KEY,
                Regions.EU_WEST_2,
                null,
                SOURCE_QUEUE_NAME,
                ""
                );

        eventEmitterSQS = injector.getInstance(EventEmitterSQS.class);
        objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Test
    public void shouldPutMessageOnQueue() {

        eventEmitterSQS.record(new AuditEvent() {
            @Override
            public String getEventId() {
                return "12345678";
            }

            @Override
            public DateTime getTimestamp() {
                return DateTime.now();
            }

            @Override
            public String getLoggableMessage() { return "Loggable Message"; }
        });

    }

}
