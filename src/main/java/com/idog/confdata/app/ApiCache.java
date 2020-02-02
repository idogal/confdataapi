package com.idog.confdata.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.idog.confdata.beans.AcademicApiPaper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiCache {

    private Cache<String, List<AcademicApiPaper>> academicApiPapers;

    public ApiCache() {        
        academicApiPapers
                = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        //.weakKeys()
                        .expireAfterAccess(60, TimeUnit.MINUTES)
                        .build();        
    }

    public Cache<String, List<AcademicApiPaper>> getAcademicApiPapers() {
        return academicApiPapers;
    }
    
    public List<AcademicApiPaper> getAcademicApiPaper(String id) {
        return academicApiPapers.getIfPresent(id);
    }    
    
    public void putAcademicApiPapers(String id, List<AcademicApiPaper> value) {
        academicApiPapers.put(id, value);
    }    
}
