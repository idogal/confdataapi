package com.idog.confdata.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ApiCache {
    
    private static final Logger LOGGER = LogManager.getLogger("VisApi");
    private final Cache<String, List<AcademicApiPaper>> academicApiPapers;
    private final Cache<Integer, Pair<Integer, List<Future<ExpandResult>>>> expandQueue;
    private final Cache<Integer, Set<AcademicApiAuthor>> authorsCache;
    private final Cache<Integer, List<AcademicApiPaper>> papersCache;
    private final Cache<Integer, List<AcademicBibliographicCouplingItem>> abcCouplingResults;
    private volatile Set<AcademicApiAuthor> chaseAuthors = null;
    private volatile List<AcademicApiPaper> chasePapers = null;

    public ApiCache() {        
        academicApiPapers
                = CacheBuilder.newBuilder()
                        .maximumSize(2000)
                        //.weakKeys()
                        .expireAfterAccess(4, TimeUnit.HOURS)
                        .build();

        expandQueue = CacheBuilder.newBuilder()
                .maximumSize(50)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();

        authorsCache = CacheBuilder.newBuilder()
                .maximumSize(50)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();

        papersCache = CacheBuilder.newBuilder()
                .maximumSize(50)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();

        abcCouplingResults = CacheBuilder.newBuilder()
                .maximumSize(50)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();

        LOGGER.info("Initialized ApiCache, academicApiPapers is set with [maxSize={}], [expireAfterAccess={}]", 1000, "60, TimeUnit.MINUTES");
    }

    public void clearAll() {
        this.academicApiPapers.invalidateAll();
        this.authorsCache.invalidateAll();
        this.papersCache.invalidateAll();
        this.expandQueue.invalidateAll();
        this.abcCouplingResults.invalidateAll();
        this.chaseAuthors = null;
        this.chasePapers = null;
    }

    public Pair<Integer, List<Future<ExpandResult>>> getExpandQueue(int key) {
        return expandQueue.getIfPresent(key);
    }

    public int putInExpandQueue(int key, int size, List<Future<ExpandResult>> value) {
        expandQueue.put(key, new ImmutablePair<>(size, value));
        return key;
    }

    public Set<AcademicApiAuthor> getAuthors(int key) {
        return authorsCache.getIfPresent(key);
    }

    public List<AcademicApiPaper> getPapers(int key) {
        return papersCache.getIfPresent(key);
    }

    public void cacheAuthors(int key, Set<AcademicApiAuthor> value) {
        authorsCache.put(key, value);
    }

    public void cachePapers(int key, List<AcademicApiPaper> value) {
        papersCache.put(key, value);
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

    public synchronized List<AcademicBibliographicCouplingItem> getAbcCouplingResults(int key) {
        LOGGER.info("Getting ABC Coupling Results from Cache");
        return abcCouplingResults.getIfPresent(key);
    }

    public synchronized void setAbcCouplingResults(int key, List<AcademicBibliographicCouplingItem> abcCouplingResults) {
        this.abcCouplingResults.put(key, abcCouplingResults);
        LOGGER.info("Populated Cache with ABC Coupling Results");
    }
}
