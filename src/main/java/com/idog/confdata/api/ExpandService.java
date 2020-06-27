package com.idog.confdata.api;

import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public enum ExpandService {

    INSTANCE;

    private final VisMsApiService visMsApiService = new VisMsApiService();
    private ExecutorService executorService = null;
    private Map<Integer, List<Future<ExpandResult>>> tasks = new HashMap<>();

    public void clearTasks() {
        this.cancelAll();
        this.tasks = null;
    }

    public List<Future<ExpandResult>> expandAsync(int hash, List<AcademicApiPaper> academicApiPapers, Set<AcademicApiAuthor> authors) {
        if (tasks.get(hash) != null)
            return Collections.emptyList();

        executorService = Executors.newFixedThreadPool(1);
        List<Future<ExpandResult>> currentTasks = new ArrayList<>();

        authors.forEach(author ->
                currentTasks.add(executorService.submit(() -> expand(academicApiPapers, author)))
        );
        tasks.put(hash, currentTasks);

        executorService.shutdown();

        return currentTasks;
    }

    public List<Future<ExpandResult>> getTasks(int hash) {
        return tasks.get(hash);
    }

    public long checkStatus(int hash) {
        if (executorService == null)
            return 0L;

        return tasks.get(hash).stream().filter(Future::isDone).filter(t -> !t.isCancelled()).count();
    }

    public void cancelAll() {
        if (executorService == null || executorService.isTerminated())
            return;

        tasks.values().forEach(tasks ->
                tasks.forEach(task -> {
                    if (!task.isDone() && !task.isCancelled()) {
                        task.cancel(true);
                    }
                })
        );
    }

    private ExpandResult expand(List<AcademicApiPaper> academicApiPapers, AcademicApiAuthor author) {
        List<Long> refsForAuthor = academicApiPapers.stream()
                .filter(p -> p.getAuthors().contains(author))
                .map(AcademicApiPaper::getReferences)
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        return new ExpandResult(author, visMsApiService.fetchById(refsForAuthor));
    }
}
