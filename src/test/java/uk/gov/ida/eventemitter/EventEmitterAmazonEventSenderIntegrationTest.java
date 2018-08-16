package uk.gov.ida.eventemitter;

import com.amazonaws.Response;
import com.amazonaws.regions.Regions;
import httpstub.ExpectedRequest;
import httpstub.HttpStubRule;
import httpstub.RegisteredResponse;
import org.assertj.core.api.Fail;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.net.URI;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ida.eventemitter.utils.TestEventBuilder.aTestEventMessage;

public class EventEmitterAmazonEventSenderIntegrationTest extends EventEmitterTestBaseConfiguration {

    private static final boolean CONFIGURATION_ENABLED = true;
    private static final String DUMMY_AWS_ACCESS_KEY = "ABCD1234EFGH5678IJKL";
    private static final String DUMMY_AWS_SECRET_ACCESS_KEY = "abcd1234DEFH5678ijkl4321mnop8765qrst1234";

    @ClassRule
    public static HttpStubRule apiGatewayStub = new HttpStubRule();

    @AfterClass
    public static void tearDown() {
        apiGatewayStub.reset();
    }

    @Test
    public void shouldReturnHTTP403WhenCredentialsMissing() throws Exception {

        createTestConfiguration(
                CONFIGURATION_ENABLED,
                "",
                "",
                Regions.EU_WEST_2,
                URI.create(apiGatewayStub.baseUri().build().toString() + AUDIT_EVENTS_API_RESOURCE)
        );

        final RegisteredResponse expectedResponse = new RegisteredResponse(
                403,
                "application/json",
                "hello",
                createTestResponseHeadersMap()
        );
        final EventSender amazonEventSender = injector.getInstance(EventSender.class);
        final Encrypter encrypter = injector.getInstance(Encrypter.class);
        final Event event = aTestEventMessage().build();
        final String encryptedEvent = encrypter.encrypt(event);
        final ExpectedRequest expectedRequest = new ExpectedRequest(AUDIT_EVENTS_API_RESOURCE, "POST", null, encryptedEvent);

        try {
            apiGatewayStub.register(expectedRequest, expectedResponse);
            Response<Void> response = amazonEventSender.sendAuthenticated(event, encryptedEvent);
            Fail.fail("Should return Forbidden (403)");
        } catch (AwsResponseException e) {
            assertThat(e.getResponse().getStatusCode()).isEqualTo(403);
        }
    }

    @Test
    public void shouldReturnHTTP200WhenAuthenticationSucceeds() throws Exception {

        createTestConfiguration(
                CONFIGURATION_ENABLED,
                DUMMY_AWS_ACCESS_KEY,
                DUMMY_AWS_SECRET_ACCESS_KEY,
                Regions.EU_WEST_2,
                URI.create(apiGatewayStub.baseUri().build().toString() + AUDIT_EVENTS_API_RESOURCE)
        );

        final RegisteredResponse expectedResponse = new RegisteredResponse(
                200,
                "application/json",
                "hello",
                createTestResponseHeadersMap()
        );
        final EventSender amazonEventSender = injector.getInstance(EventSender.class);
        final Encrypter encrypter = injector.getInstance(Encrypter.class);
        final Event event = aTestEventMessage().build();
        final String encryptedEvent = encrypter.encrypt(event);

        /*
            Expected headers not provided as HttpStub requires both header names and content to match, and content is dynamic.
            The expected headers are tested separated using assertions in the try block below.
         */
        final ExpectedRequest expectedRequest =
                new ExpectedRequest(AUDIT_EVENTS_API_RESOURCE, "POST", null, encryptedEvent);

        try {
            apiGatewayStub.register(expectedRequest, expectedResponse);
            Response<Void> response = amazonEventSender.sendAuthenticated(event, encryptedEvent);
            assertThat(response.getHttpResponse().getStatusCode()).isEqualTo(200);

            /*
                Check that all the headers required for AWS Authentication and AWS4 Signing are present.
                Header contents are not validated.
             */
            assertThat(apiGatewayStub.getLastRequest().getHeader("Host")).isNotNull();
            assertThat(apiGatewayStub.getLastRequest().getHeader("Authorization")).isNotNull();

            // Authorization header must contain all of the following keywords...
            assertThat(apiGatewayStub.getLastRequest().getHeader("Authorization")).containsPattern(Pattern.compile("(?=.*AWS4-HMAC-SHA256)(?=.*Signature)(?=.*Credential)"));
            assertThat(apiGatewayStub.getLastRequest().getHeader("Content-type")).isEqualToIgnoringCase("application/json");
            assertThat(apiGatewayStub.getLastRequest().getHeader("X-Amz-Date")).isNotNull();

        } catch (AwsResponseException e) {
            Fail.fail("Should return Success (200): " + e.getMessage());
        }
    }

    @Test(expected = AwsResponseException.class)
    public void shouldThrowAwsResponseExceptioWhenNotFound() throws java.io.UnsupportedEncodingException {

        createTestConfiguration(
                CONFIGURATION_ENABLED,
                DUMMY_AWS_ACCESS_KEY,
                DUMMY_AWS_SECRET_ACCESS_KEY,
                Regions.EU_WEST_2,
                URI.create(apiGatewayStub.baseUri().build().toString() + "")
        );

        apiGatewayStub.register(AUDIT_EVENTS_API_RESOURCE, 403);
        final EventSender amazonEventSender = injector.getInstance(EventSender.class);
        final Encrypter encrypter = injector.getInstance(Encrypter.class);
        final Event event = aTestEventMessage().build();

        Response<Void> response = amazonEventSender.sendAuthenticated(event, "");

    }


}

