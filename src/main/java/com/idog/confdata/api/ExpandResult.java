package com.idog.confdata.api;

import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.List;
import java.util.Objects;

public class ExpandResult {
    private AcademicApiAuthor author;
    private List<AcademicApiPaper> papers;

    public ExpandResult(AcademicApiAuthor author, List<AcademicApiPaper> papers) {
        this.author = author;
        this.papers = papers;
    }

    public AcademicApiAuthor getAuthor() {
        return author;
    }

    public List<AcademicApiPaper> getPapers() {
        return papers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpandResult that = (ExpandResult) o;
        return author.equals(that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author);
    }
}
