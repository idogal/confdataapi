package com.idog.confdata.api;

import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public enum PaperExpandService {

    INSTANCE;

    private final VisMsApiService visMsApiService = new VisMsApiService();
    private ExecutorService executorService = null;
    private Map<Integer, Future<Set<AcademicApiPaper>>> paperExpansionTasks = new HashMap<>();

    public void clearTasks() {
        this.cancelAll();
        this.paperExpansionTasks = null;
    }

    public Optional<Future<Set<AcademicApiPaper>>> expandPapersAsync(int hash, List<AcademicApiPaper> academicApiPapers) {
        if (paperExpansionTasks.get(hash) != null)
            return Optional.empty();

        executorService = Executors.newFixedThreadPool(1);

        Future<Set<AcademicApiPaper>> submitionResults = executorService.submit(() -> expandPapersOnly(academicApiPapers));

        paperExpansionTasks.put(hash, submitionResults);
        executorService.shutdown();

        return Optional.of(submitionResults);
    }

    public Future<Set<AcademicApiPaper>> getTasks(int hash) {
        return paperExpansionTasks.get(hash);
    }

    public long checkStatus(int hash) {
        if (executorService == null)
            return 0L;

        Future<Set<AcademicApiPaper>> listFuture = paperExpansionTasks.get(hash);
        if (listFuture.isCancelled()) {
            return 0;
        }

        if (listFuture.isDone()) {
            return 1;
        }

        return 1;
    }

    public void cancelAll() {
        if (executorService == null || executorService.isTerminated())
            return;

        paperExpansionTasks.values().forEach(task -> {
            if (!task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        });
    }

    private Set<AcademicApiPaper> expandPapersOnly(List<AcademicApiPaper> academicApiPapers) {
        List<Long> refs = academicApiPapers.stream()
                .map(AcademicApiPaper::getReferences)
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        return new HashSet<>(visMsApiService.fetchById(refs));
    }
}
