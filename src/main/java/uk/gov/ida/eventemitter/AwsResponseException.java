package uk.gov.ida.eventemitter;

import com.amazonaws.SdkBaseException;
import com.amazonaws.http.HttpResponse;

public class AwsResponseException extends SdkBaseException {
    private HttpResponse response;

    public AwsResponseException(HttpResponse response) {
        super(response.getStatusText());
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
