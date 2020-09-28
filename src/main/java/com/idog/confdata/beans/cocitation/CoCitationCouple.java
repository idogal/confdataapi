package com.idog.confdata.beans.cocitation;

import com.idog.confdata.beans.api.AcademicApiPaper;

import java.util.Objects;

public class CoCitationCouple {
    private final AcademicApiPaper paperA;
    private final AcademicApiPaper paperB;

    public CoCitationCouple(AcademicApiPaper paperA, AcademicApiPaper paperB) {
        this.paperA = paperA;
        this.paperB = paperB;
    }

    public AcademicApiPaper getPaperA() {
        return paperA;
    }

    public AcademicApiPaper getPaperB() {
        return paperB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoCitationCouple that = (CoCitationCouple) o;
        return Objects.equals(paperA.getId(), that.paperA.getId()) &&
                Objects.equals(paperB.getId(), that.paperB.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(paperA.getId(), paperB.getId());
    }
}
