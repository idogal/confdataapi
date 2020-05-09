package com.idog.confdata.app;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.idog.confdata.beans.api.AcademicApiResponse;
import com.idog.confdata.beans.api.AcademicApiResponseDeserializer;

import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

@Singleton
public class VisServerAppResourcesImpl implements VisServerAppResources {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("VisApi");

    private ObjectMapper mapper;
    private final ApiCache msApiCache = new ApiCache();
    private final DiskStorage diskStorage;
    
    // @Inject
    public VisServerAppResourcesImpl() throws IOException {
        diskStorage = new DiskStorageService("C:\\Users\\idoga\\Documents\\Dev\\confdata\\src\\main\\resources\\json_files");

        mapper = new ObjectMapper();

        LOGGER.info("Initialising the VisServerAppResources object");

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(AcademicApiResponse.class, new AcademicApiResponseDeserializer());
        mapper.registerModule(module);

        LOGGER.trace("Configured the ObjectMapper for: FAIL_ON_UNKNOWN_PROPERTIES");
        LOGGER.trace("Registered a Deserializer: AcademicApiResponseDeserializer");

        LOGGER.info("Finished initialising the VisServerAppResources object");
    }
    
    @Override
    public ObjectMapper getMapper() {
        return this.mapper;
    }

    @Override
    public ApiCache getApiCache() {
        return this.msApiCache;
    }

    @Override
    public DiskStorage getDiskStorage() {
        return diskStorage;
    }
}
