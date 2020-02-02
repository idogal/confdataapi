package com.idog.confdata.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.idog.confdata.beans.AcademicApiPaper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiCache {

    private Cache<String, List<AcademicApiPaper>> apiByIdResponses;

    public ApiCache() {        
        apiByIdResponses
                = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        //.weakKeys()
                        .expireAfterAccess(60, TimeUnit.MINUTES)
                        .build();        
    }

    public Cache<String, List<AcademicApiPaper>> getByIdResponses() {
        return apiByIdResponses;
    }
    
    public List<AcademicApiPaper> getByIdResponse(String id) {
        return apiByIdResponses.getIfPresent(id);
    }    
    
    public void putByIdResponse(String id, List<AcademicApiPaper> value) {
        apiByIdResponses.put(id, value);
    }    
}
