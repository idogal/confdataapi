package com.idog.confdata.api;

import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.List;

public class PaperExpandResults {
    private List<AcademicApiPaper> papers;

    public PaperExpandResults(List<AcademicApiPaper> papers) {
        this.papers = papers;
    }

    public List<AcademicApiPaper> getPapers() {
        return papers;
    }
}
