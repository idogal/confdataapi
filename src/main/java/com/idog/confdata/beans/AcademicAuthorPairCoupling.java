package com.idog.confdata.beans;

import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.Set;

public class AcademicAuthorPairCoupling implements Comparable<AcademicAuthorPairCoupling> {
    private AcademicAuthorPair authors;
    private int coupling;
    private Set<AcademicApiPaper> paperSet;

    public AcademicAuthorPairCoupling(AcademicAuthorPair authors, int coupling, Set<AcademicApiPaper> paperSet) {
        this.authors = authors;
        this.coupling = coupling;
        this.paperSet = paperSet;
    }

    public AcademicAuthorPair getAuthors() {
        return authors;
    }

    public int getCoupling() {
        return coupling;
    }

    public Set<AcademicApiPaper> getPaperSet() {
        return paperSet;
    }

    @Override
    public String toString() {
        return authors + ", " + coupling + ", " + paperSet;
    }

    @Override
    public int compareTo(AcademicAuthorPairCoupling o) {
        return this.authors.compareTo(o.authors);
    }
}
