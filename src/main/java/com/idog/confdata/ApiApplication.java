package com.idog.confdata;

import javax.ws.rs.ApplicationPath;

import com.idog.confdata.resources.AcademicData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class ApiApplication extends ResourceConfig {
	private static final Logger LOGGER = LogManager.getLogger("VisApi");

	public ApiApplication() {
        super (
            AcademicData.class,
            JacksonFeature.class
        );
        
        // this.register(new VisServerAppBinder());
        // this.register(new VisServerAppRequestBinder());        
		// this.register(new CORSFilter());
		
		System.setProperty("log4j.configurationFile", "C:\\Users\\idoga\\Documents\\Dev\\confdata\\src\\main\\resources\\log4j2.xml");
		LOGGER.info("Created the ApiApplication");
	}

}
