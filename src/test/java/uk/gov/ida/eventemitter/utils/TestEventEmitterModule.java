package uk.gov.ida.eventemitter.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class TestEventEmitterModule extends AbstractModule {

    public TestEventEmitterModule() {
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }
}
