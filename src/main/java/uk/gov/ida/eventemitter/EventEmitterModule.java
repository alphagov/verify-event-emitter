package uk.gov.ida.eventemitter;

import com.google.inject.AbstractModule;

public class EventEmitterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EventEmitter.class);
    }
}
