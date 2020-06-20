package com.idog.confdata.app;

import com.google.common.collect.Sets;
import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.api.ExpandService;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.CouplingResult;
import com.idog.confdata.beans.CouplingResultType;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.AcademicAuthorPair;
import com.idog.confdata.beans.api.AcademicApiPaper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CouplingServiceUtils {

    private static final Logger LOGGER = LogManager.getLogger(CouplingServiceUtils.class);

    public static Set<AcademicAuthorPair> getAuthorPairs(Set<AcademicApiAuthor> authors) {
        Set<AcademicAuthorPair> newAuthors = new HashSet<>();

        for (List<AcademicApiAuthor> tuple : Sets.cartesianProduct(authors, authors)) {
            AcademicApiAuthor firstElement = tuple.get(0);
            AcademicApiAuthor secondElement = tuple.get(1);

            if (!firstElement.getAuthorName().equals(secondElement.getAuthorName()))
                newAuthors.add(new AcademicAuthorPair(firstElement, secondElement));
        }

        return newAuthors;
    }

    public static List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplingsFromExpand(List<AcademicApiPaper> academicApiPapers,
                                                                                                    Set<AcademicApiAuthor> authors,
                                                                                                    Map<AcademicApiAuthor, List<AcademicApiPaper>> refsPerAuthor) {
        List<AcademicBibliographicCouplingItem> couplings = new ArrayList<>();
        getAuthorPairs(authors).forEach(pair -> {
            AcademicApiAuthor academicApiAuthorFirst = pair.getAcademicApiAuthorFirst();
            AcademicApiAuthor academicApiAuthorSecond = pair.getAcademicApiAuthorSecond();

            Set<Long> refsFromJointDocs = academicApiPapers.stream()
                    .filter(p -> p.getAuthors().contains(academicApiAuthorFirst) && p.getAuthors().contains(academicApiAuthorSecond))
                    .map(AcademicApiPaper::getReferences)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            List<AcademicApiPaper> papersThatWereReferencedByA =
                    refsPerAuthor.get(academicApiAuthorFirst).stream()
                            .filter(p -> !refsFromJointDocs.contains(p.getId()))
                            .collect(Collectors.toList());

            List<AcademicApiPaper> papersThatWereReferencedByB =
                    refsPerAuthor.get(academicApiAuthorSecond).stream()
                            .filter(p -> !refsFromJointDocs.contains(p.getId()))
                            .collect(Collectors.toList());

            Map<String, List<AcademicApiPaper>> a = papersThatWereReferencedByA.stream().collect(Collectors.groupingBy(AcademicApiPaper::getTitle));
            Map<String, List<AcademicApiPaper>> b = papersThatWereReferencedByB.stream().collect(Collectors.groupingBy(AcademicApiPaper::getTitle));

            List<Integer> weights = new ArrayList<>();
            Sets.SetView<String> intersection = Sets.intersection(a.keySet(), b.keySet());
            intersection.forEach(intersected -> {
                int min = Math.min(a.get(intersected).size(), b.get(intersected).size());
                weights.add(min);
            });

            int sum = weights.stream().mapToInt(i -> i).sum();
            if (sum > 0)
                couplings.add(new AcademicBibliographicCouplingItem(academicApiAuthorFirst, academicApiAuthorSecond, sum));
        });

        return couplings;
    }

    static CouplingResult getQueuedResults(List<AcademicApiPaper> papers, Set<AcademicApiAuthor> authors, Map<AcademicApiAuthor, List<AcademicApiPaper>> refsPerAuthor) {
        return CouplingResult.builder()
                .setResultType(CouplingResultType.SUCCESS)
                .setCouplings(getAuthorBibliographicCouplingsFromExpand(papers, authors, refsPerAuthor))
                .build();
    }

//    static int queueExpandData(List<AcademicApiPaper> papers, Set<AcademicApiAuthor> authors) {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//        ExpandService expandService = ExpandService.INSTANCE;
//        Future<List<Future<ExpandResult>>> expandRequest = executorService.submit(() -> expandService.expandAsync(papers, authors));
//        try {
//            ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
//            return apiCache.putInExpandQueue(authors.size(), expandRequest.get());
//        } catch (Exception ex) {
//            return 0;
//        }
//    }

    public static List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplings(List<AcademicApiPaper> papers, Set<AcademicApiAuthor> authors)
            throws DataNotYetReadyException {

        List<Future<ExpandResult>> tasks = ExpandService.INSTANCE.getTasks();
        if (tasks == null) {
            ExpandService expandService = ExpandService.INSTANCE;
            List<Future<ExpandResult>> futures = expandService.expandAsync(papers, authors);
            throw new DataNotYetReadyException("Initial network request. Preparing the data [" + futures.stream()  + "] requests...");
        }

        long doneExpands = tasks.stream().filter(Future::isDone).filter(t -> !t.isCancelled()).count();
        if (doneExpands != authors.size()) {
            throw new DataNotYetReadyException("Papers expansion is still running. Please wait for it to finish first.");
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

        return getAuthorBibliographicCouplingsFromExpand(papers, authors, refsPerAuthor);
    }

}
