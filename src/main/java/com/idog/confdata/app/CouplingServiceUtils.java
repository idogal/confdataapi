package com.idog.confdata.app;

import com.google.common.collect.Sets;
import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.api.ExpandService;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.AcademicAuthorPair;
import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CouplingServiceUtils {

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

    public static List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplings(List<AcademicApiPaper> academicApiPapers, Set<AcademicApiAuthor> authors) {
        HashMap<AcademicApiAuthor, Set<AcademicApiPaper>> papersPerAuthor = new HashMap<>();
        academicApiPapers.forEach(p ->
                p.getAuthors().forEach(a -> {
                    Set<AcademicApiPaper> papersForCurrentAuthor = papersPerAuthor.getOrDefault(a, new HashSet<>());
                    papersForCurrentAuthor.add(p);
                    papersPerAuthor.putIfAbsent(a, papersForCurrentAuthor);
                })
        );

        List<Future<ExpandResult>> tasks = ExpandService.INSTANCE.getTasks();
        if (tasks == null) {
            throw new IllegalStateException("Can't get papers expansion results. Please check if the 'expand' API has been executed.");
        }

        long doneExpands = tasks.stream().filter(Future::isDone).filter(t -> !t.isCancelled()).count();
        if (doneExpands != authors.size()) {
            throw new IllegalStateException("Papers expansion is still running. Please wait for it to finish first.");
        }

        Map<AcademicApiAuthor, List<AcademicApiPaper>> refsPerAuthor = tasks.stream()
                .filter(Future::isDone)
                .filter(t -> !t.isCancelled())
                .map(t -> {
                    try {
                        return t.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(ExpandResult::getAuthor, ExpandResult::getPapers));

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

}
