package com.idog.confdata.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface VisServerAppResources {

    ObjectMapper getMapper();

    ApiCache getApiCache();
}