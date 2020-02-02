package com.idog.confdata.api;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Binds singletons
 * 
 * @author idog
 */
public class VisServerAppBinder extends AbstractModule {

    @Override
    protected void configure() {
        bind(VisServerAppResources.class).to(VisServerAppResourcesImpl.class).in(Singleton.class);
    }
}
