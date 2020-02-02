package com.idog.confdata.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.idog.confdata.api.VisServerAppBinder;

public final class DiResources {

    private static final Injector injector;

    private DiResources() {
    }

    static {
        injector = Guice.createInjector(new VisServerAppBinder());
    }

    public static Injector getInjector() {
        return injector;
    }


}