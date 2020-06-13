package com.idog.confdata;

import javax.ws.rs.ApplicationPath;

import com.idog.confdata.resources.AcademicDataResource;

import com.idog.confdata.resources.AdminResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class ApiApplication extends ResourceConfig {
    private static final Logger LOGGER = LogManager.getLogger("VisApi");

    public ApiApplication() {
        super(
                AcademicDataResource.class,
                AdminResource.class,
                JacksonFeature.class
        );

        // this.register(new VisServerAppBinder());
        // this.register(new VisServerAppRequestBinder());        
        // this.register(new CORSFilter());

        LOGGER.info("Created the ApiApplication");
    }

}
