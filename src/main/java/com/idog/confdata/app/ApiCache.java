package com.idog.confdata.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ApiCache {
    
    private static final Logger LOGGER = LogManager.getLogger("VisApi");
    private final Cache<String, List<AcademicApiPaper>> academicApiPapers;
    private volatile Set<AcademicApiAuthor> chaseAuthors = null;
    private volatile List<AcademicApiPaper> chasePapers = null;
    private volatile List<AcademicBibliographicCouplingItem> abcCouplingResults = null;

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

    public synchronized Set<AcademicApiAuthor> getChaseAuthors() {
        return chaseAuthors;
    }

    public synchronized void setChaseAuthors(Set<AcademicApiAuthor> chaseAuthors) {
        this.chaseAuthors = chaseAuthors;
        LOGGER.info("Populated Cache with Authors Set");
    }

    public synchronized List<AcademicApiPaper> getChasePapers() {
        LOGGER.info("Getting Papers List from Cache");
        return chasePapers;
    }

    public synchronized void setChasePapers(List<AcademicApiPaper> chasePapers) {
        this.chasePapers = chasePapers;
        LOGGER.info("Populated Cache with Papers List");
    }

    public synchronized List<AcademicBibliographicCouplingItem> getAbcCouplingResults() {
        LOGGER.info("Getting ABC Coupling Results from Cache");
        return abcCouplingResults;
    }

    public synchronized void setAbcCouplingResults(List<AcademicBibliographicCouplingItem> abcCouplingResults) {
        this.abcCouplingResults = abcCouplingResults;
        LOGGER.info("Populated Cache with ABC Coupling Results");
    }
}
