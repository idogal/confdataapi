package com.idog.confdata.api;

import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public enum ExpandService {

    INSTANCE;

    private VisMsApiService visMsApiService = new VisMsApiService();
    private ExecutorService executorService = null;
    private List<Future<ExpandResult>> tasks = null;

    public List<Future<ExpandResult>> expandAsync(List<AcademicApiPaper> academicApiPapers, Set<AcademicApiAuthor> authors) {
        // TODO: Allow to expand again
        if (tasks != null)
            return Collections.emptyList();

        executorService = Executors.newFixedThreadPool(1);
        this.tasks = new ArrayList<>();

        authors.forEach(author ->
            this.tasks.add(executorService.submit(() -> expand(academicApiPapers, author)))
        );

        executorService.shutdown();

        return this.tasks;
    }

    public List<Future<ExpandResult>> getTasks() {
        return tasks;
    }

    public long checkStatus() {
        if (executorService == null)
            return 0L;

        return tasks.stream().filter(t -> t.isDone()).filter(t -> !t.isCancelled()).count();
    }

    public void cancelAll() {
        // TODO: get cancelled count

        if (executorService == null || executorService.isTerminated())
            return;

        tasks.forEach(task -> {
            if (!task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        });
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
