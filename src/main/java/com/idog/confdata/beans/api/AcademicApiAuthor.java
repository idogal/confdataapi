package com.idog.confdata.beans.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class AcademicApiAuthor implements Comparable<AcademicApiAuthor> {

    @JsonProperty("AuN") private String authorName;
    @JsonProperty("AuId") private Long authorId;
    @JsonProperty("AfN") private String affiliationName;
    @JsonProperty("AfId") private Long affiliationId;
    @JsonProperty("S") private Integer paperOrder;

    public AcademicApiAuthor(String authorName, Long authorId, String affiliationName, Long affiliationId, Integer paperOrder) {
        if (authorName == null)
            throw new IllegalArgumentException("authorName cannot be null.");

        this.authorName = authorName;
        this.authorId = authorId;
        this.affiliationName = affiliationName;
        this.affiliationId = affiliationId;
        this.paperOrder = paperOrder;
    }
    
    public Long getAffiliationId() {
        return affiliationId;
    }

    public String getAffiliationName() {
        return affiliationName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Integer getPaperOrder() {
        return paperOrder;
    }

    public void setAffiliationId(Long affiliationId) {
        this.affiliationId = affiliationId;
    }

    public void setAffiliationName(String affiliationName) {
        this.affiliationName = affiliationName;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setPaperOrder(Integer paperOrder) {
        this.paperOrder = paperOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcademicApiAuthor that = (AcademicApiAuthor) o;
        return authorName.equals(that.authorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorName);
    }

    @Override
    public String toString() {
        return authorName;
    }

    @Override
    public int compareTo(AcademicApiAuthor o) {
        return this.getAuthorName().compareTo(o.getAuthorName());
    }
}
