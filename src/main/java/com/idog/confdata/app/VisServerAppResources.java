package com.idog.confdata.app;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface VisServerAppResources {

    ObjectMapper getMapper();

    ApiCache getApiCache();

    DiskStorage getDiskStorage();
}