package com.idog.confdata.app;

import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.api.ExpandService;
import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.CouplingResult;
import com.idog.confdata.beans.CouplingResultType;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CouplingService {
    private static final Logger LOGGER = LogManager.getLogger(CouplingService.class);

    VisMsApiService visMsApiService = new VisMsApiService();

    public CouplingResult getQueuedAuthorBibliographicCouplingsResults(int num) {
        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        List<AcademicApiPaper> papers = apiCache.getPapers(num);
        Set<AcademicApiAuthor> authors = apiCache.getAuthors(num);

        if (papers == null) {
            throw new RuntimeException("Can't get papers for computation");
        }

        if (authors == null) {
            throw new RuntimeException("Can't get papers for computation");
        }

        Pair<Integer, List<Future<ExpandResult>>> expandQueuePair = apiCache.getExpandQueue(num);
        Integer expectedSize = expandQueuePair.getKey();

        List<Future<ExpandResult>> tasks = expandQueuePair.getValue();
        if (tasks == null) {
            LOGGER.error("cant get tasks");
            return CouplingResult.builder()
                    .setResultType(CouplingResultType.FAILURE)
                    .setMessage("cant get tasks").build();
        }

        long doneExpands = tasks.stream().filter(Future::isDone).filter(t -> !t.isCancelled()).count();
        if (doneExpands != expectedSize) {
            String msg = String.format("Papers expansion is still running. Please wait for it to finish first. State: [%s/%s]", doneExpands, expectedSize);
            return CouplingResult.builder()
                    .setResultType(CouplingResultType.PENDING)
                    .setMessage(msg)
                    .build();
        }

        Map<AcademicApiAuthor, List<AcademicApiPaper>> refsPerAuthor = tasks.stream()
                .filter(Future::isDone)
                .filter(t -> !t.isCancelled())
                .map(t -> {
                    try {
                        return t.get();
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.error(e);
                    }

                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(ExpandResult::getAuthor, ExpandResult::getPapers));

        return CouplingServiceUtils.getQueuedResults(papers, authors, refsPerAuthor);
    }

    private int getHash(String yearStart, String yearEnd) {
        yearStart = (yearStart == null) ? "0" : yearStart;
        yearEnd = (yearEnd == null) ? "0" : yearEnd;
        return  (yearStart + yearEnd).hashCode();
    }

    public int queuePreparation(String yearStart, String yearEnd) {
        int hash = getHash(yearStart, yearEnd);
        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        Pair<Integer, List<Future<ExpandResult>>> expandQueueResult = apiCache.getExpandQueue(hash);
        if (expandQueueResult != null) {
            return hash;
        }

        List<AcademicApiPaper> papers = visMsApiService.getChasePapers(yearStart, yearEnd);
        Set<AcademicApiAuthor> authors = visMsApiService.deriveChaseAuthors(papers);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        ExpandService expandService = ExpandService.INSTANCE;
        Future<List<Future<ExpandResult>>> expandRequest = executorService.submit(() -> expandService.expandAsync(hash, papers, authors));
        try {
            apiCache.cachePapers(hash, papers);
            apiCache.cacheAuthors(hash, authors);
            return apiCache.putInExpandQueue(hash, authors.size(), expandRequest.get());
        } catch (Exception ex) {
            return 0;
        }
    }

    public List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplingsResults(String yearStart, String yearEnd) throws DataNotYetReadyException {
        VisServerAppResources instance = DiResources.getInjector().getInstance(VisServerAppResources.class);
        ApiCache apiCache = instance.getApiCache();

        int hash = getHash(yearStart, yearEnd);
        List<AcademicBibliographicCouplingItem> couplings = apiCache.getAbcCouplingResults(hash);
        if (couplings != null) {
            return couplings;
        }

        List<AcademicApiPaper> papers = apiCache.getPapers(hash);
        Set<AcademicApiAuthor> authors = apiCache.getAuthors(hash);
        List<AcademicBibliographicCouplingItem> authorBibliographicCouplings = CouplingServiceUtils.getAuthorBibliographicCouplings(hash, papers, authors);
        apiCache.setAbcCouplingResults(hash, authorBibliographicCouplings);

        return authorBibliographicCouplings;
    }
}
