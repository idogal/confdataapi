package com.idog.confdata.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idog.confdata.beans.api.AcademicApiAuthor;

public class AcademicBibliographicCouplingItem implements Comparable<AcademicBibliographicCouplingItem> {

    @JsonProperty("firstAuthor")  private AcademicApiAuthor academicApiAuthorFirst;
    @JsonProperty("secondAuthor")  private AcademicApiAuthor academicApiAuthorSecond;
    @JsonProperty("couplingStrength")  private int couplingStrength;

    public AcademicBibliographicCouplingItem(AcademicApiAuthor academicApiAuthorFirst, AcademicApiAuthor academicApiAuthorSecond, int couplingStrength) {
        this.academicApiAuthorFirst = academicApiAuthorFirst;
        this.academicApiAuthorSecond = academicApiAuthorSecond;
        this.couplingStrength = couplingStrength;
    }

    public AcademicApiAuthor getAcademicApiAuthorFirst() {
        return academicApiAuthorFirst;
    }

    public AcademicApiAuthor getAcademicApiAuthorSecond() {
        return academicApiAuthorSecond;
    }

    public int getCouplingStrength() {
        return couplingStrength;
    }

    @Override
    public int compareTo(AcademicBibliographicCouplingItem o) {
        return this.getAcademicApiAuthorFirst().getAuthorName().compareTo(o.getAcademicApiAuthorFirst().getAuthorName());
    }
}
