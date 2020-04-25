package com.idog.confdata.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.idog.confdata.beans.api.AcademicApiPaper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiCache {
    
    private static final Logger LOGGER = LogManager.getLogger("VisApi");
    private Cache<String, List<AcademicApiPaper>> academicApiPapers;

    public ApiCache() {        
        academicApiPapers
                = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        //.weakKeys()
                        .expireAfterAccess(60, TimeUnit.MINUTES)
                        .build();
                        
        LOGGER.info("Initialized ApiCache, academicApiPapers is set with [maxSize={}], [expireAfterAccess={}]", 1000, "60, TimeUnit.MINUTES");
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
