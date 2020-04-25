package com.idog.confdata.beans;

import com.idog.confdata.beans.api.AcademicApiAuthor;

import java.util.Objects;

public class AcademicAuthorPair implements Comparable<AcademicAuthorPair> {
    private AcademicApiAuthor academicApiAuthorFirst;
    private AcademicApiAuthor academicApiAuthorSecond;

    public AcademicAuthorPair(AcademicApiAuthor academicApiAuthorFirst, AcademicApiAuthor academicApiAuthorSecond) {
        if (academicApiAuthorFirst == null || academicApiAuthorSecond == null)
            throw new IllegalArgumentException("All authors must be defined.");

        this.academicApiAuthorFirst = academicApiAuthorFirst;
        this.academicApiAuthorSecond = academicApiAuthorSecond;
    }

    public AcademicApiAuthor getAcademicApiAuthorFirst() {
        return academicApiAuthorFirst;
    }

    public AcademicApiAuthor getAcademicApiAuthorSecond() {
        return academicApiAuthorSecond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcademicAuthorPair that = (AcademicAuthorPair) o;
        return (academicApiAuthorFirst.equals(that.academicApiAuthorFirst) && academicApiAuthorSecond.equals(that.academicApiAuthorSecond)) ||
                (academicApiAuthorSecond.equals(that.academicApiAuthorFirst) && academicApiAuthorFirst.equals(that.academicApiAuthorSecond));
    }

    @Override
    public int hashCode() {
        return Objects.hash(academicApiAuthorFirst, academicApiAuthorSecond);
    }

    @Override
    public int compareTo(AcademicAuthorPair o) {
        return this.getAcademicApiAuthorFirst().getAuthorName()
                .compareTo(o.getAcademicApiAuthorFirst().getAuthorName());
    }

    @Override
    public String toString() {
        return academicApiAuthorFirst + ", " + academicApiAuthorSecond;
    }
}
