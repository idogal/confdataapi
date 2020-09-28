package com.idog.confdata.app;

import com.google.common.collect.Sets;
import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.api.ExpandService;
import com.idog.confdata.api.PaperExpandService;
import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.CouplingResult;
import com.idog.confdata.beans.CouplingResultType;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;
import com.idog.confdata.beans.cocitation.CoCitationCouple;
import com.idog.confdata.beans.cocitation.CoCitationResult;
import com.idog.confdata.beans.cocitation.CoCitationScore;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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

    public CoCitationResult getCoCitationResults(int hash) {
        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        Pair<Integer, Future<Set<AcademicApiPaper>>> expandQueuePair = apiCache.getExpandPapersQueue(hash);
        Integer expectedSize = expandQueuePair.getKey();

        Future<Set<AcademicApiPaper>> task = expandQueuePair.getValue();
        if (task == null) {
            LOGGER.error("cant get tasks");
            return CoCitationResult.builder()
                    .setResultType(CouplingResultType.FAILURE)
                    .setMessage("cant get tasks").build();
        }

        //long doneExpands = task.isDone() ;//tasks.stream().filter(Future::isDone).filter(t -> !t.isCancelled()).count();
        if (!task.isDone()) {
            String msg = String.format("Papers expansion is still running. Please wait for it to finish first. State: [%s/%s]", "doneExpands", expectedSize);
            return CoCitationResult.builder()
                    .setResultType(CouplingResultType.PENDING)
                    .setMessage(msg)
                    .build();
        }

        try {
            Set<AcademicApiPaper> papers1 = task.get();
            Map<Long, AcademicApiPaper> refPapers = task.get().stream().collect(Collectors.toMap(AcademicApiPaper::getId, v -> v));

            List<AcademicApiPaper> papers = apiCache.getPapers(hash);
            return getCoCitationResult(papers, refPapers);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;

//        Map<Long, AcademicApiPaper> refPapers = tasks.stream()
//                .filter(Future::isDone)
//                .filter(t -> !t.isCancelled())
//                .map(t -> {
//                    try {
//                        return t.get();
//                    } catch (InterruptedException | ExecutionException e) {
//                        LOGGER.error(e);
//                    }
//
//                    return null;
//                }).filter(Objects::nonNull)
//                .flatMap(List::stream)
//                .collect(Collectors.toMap(AcademicApiPaper::getId, v -> v));

    }

    private CoCitationResult getCoCitationResult(List<AcademicApiPaper> papers, Map<Long, AcademicApiPaper> refPapers) {
        Map<CoCitationCouple, List<AcademicApiPaper>> coCitationCouplesToOriginatingDocs = new HashMap<>();
        papers.forEach(paper -> {
            Set<Long> references = paper.getReferences();
            if (references.size() < 2) {
                return;
            }

            Sets.combinations(references, 2).forEach(combination -> {
                Iterator<Long> iterator = combination.iterator();
                Long idA = iterator.next();
                Long idB = iterator.next();
                AcademicApiPaper academicApiPaperA = refPapers.get(idA);
                AcademicApiPaper academicApiPaperB = refPapers.get(idB);

                if (academicApiPaperA == null) {
                    throw new RuntimeException("Cannot find the details for paper " + idA);
                }

                if (academicApiPaperB == null) {
                    throw new RuntimeException("Cannot find the details for paper " + idB);
                }

                coCitationCouplesToOriginatingDocs.computeIfAbsent(
                        new CoCitationCouple(academicApiPaperA, academicApiPaperB),
                        v -> new ArrayList<>()
                ).add(paper);
            });
        });


//        int minCc = (minCcStrength == null || minCcStrength < 0) ? 0 : minCcStrength;

        Map<CoCitationCouple, CoCitationScore> ccScores = coCitationCouplesToOriginatingDocs.entrySet().stream()
//                .filter(entry -> entry.getValue().size() > minCc)
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    CoCitationScore.CoCitationScoreBuilder builder = CoCitationScore.builder();
                    for (AcademicApiPaper academicApiPaper : e.getValue()) {
                        builder.addDoc(academicApiPaper.getTitle(), academicApiPaper.getId());
                    }

                    return builder.build();
                }));


        return CoCitationResult.builder().setMessage("").setResultType(CouplingResultType.SUCCESS).setCouplings(ccScores).build();
    }


    private int getHash(String yearStart, String yearEnd, Integer citationCount) {
        yearStart = (yearStart == null) ? "0" : yearStart;
        yearEnd = (yearEnd == null) ? "0" : yearEnd;
        citationCount = (citationCount == null) ? 0 : citationCount;
        return  (yearStart + yearEnd + citationCount).hashCode();
    }

    private int getCcHash(String yearStart, String yearEnd, Integer citationCount, Integer minCcStrength) {
        yearStart = (yearStart == null) ? "0" : yearStart;
        yearEnd = (yearEnd == null) ? "0" : yearEnd;
        citationCount = (citationCount == null) ? 0 : citationCount;
        minCcStrength = (minCcStrength == null) ? 0 : minCcStrength;
        return  (yearStart + yearEnd + citationCount + minCcStrength).hashCode();
    }


    public int queueCoCitationPreparation(String yearStart, String yearEnd, Integer citationCount) {
        int hash = getHash(yearStart, yearEnd, citationCount);
        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        // todo check in cache

        Pair<Integer, Future<Set<AcademicApiPaper>>> expandQueueResult = apiCache.getExpandPapersQueue(hash);
        if (expandQueueResult != null) {
            return hash;
        }

        List<AcademicApiPaper> papers = visMsApiService.getChasePapers(yearStart, yearEnd, citationCount);

        PaperExpandService expandService = PaperExpandService.INSTANCE;
        Future<Optional<Future<Set<AcademicApiPaper>>>> expandRequest = Executors.newSingleThreadExecutor().submit(() -> expandService.expandPapersAsync(hash, papers));
        try {
            apiCache.cachePapers(hash, papers);
            return apiCache.putInExpandPapersQueue(hash, papers.size(), expandRequest.get().get());
        } catch (Exception ex) {
            return 0;
        }
    }

    public int queuePreparation(String yearStart, String yearEnd, Integer citationCount) {
        int hash = getHash(yearStart, yearEnd, citationCount);
        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        Pair<Integer, List<Future<ExpandResult>>> expandQueueResult = apiCache.getExpandQueue(hash);
        if (expandQueueResult != null) {
            return hash;
        }

        List<AcademicApiPaper> papers = visMsApiService.getChasePapers(yearStart, yearEnd, citationCount);
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

    public List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplingsResults(String yearStart, String yearEnd, Integer citationCount) throws DataNotYetReadyException {
        VisServerAppResources instance = DiResources.getInjector().getInstance(VisServerAppResources.class);
        ApiCache apiCache = instance.getApiCache();

        int hash = getHash(yearStart, yearEnd, citationCount);
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
