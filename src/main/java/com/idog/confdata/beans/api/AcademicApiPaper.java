package com.idog.confdata.beans.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AcademicApiPaper {

    private Long id;
    private String title;
    private String year;
    private Integer citationCount;
    private Set<Long> references = new HashSet<>();
    private Set<String> keywords = new HashSet<>();
    private Set<AcademicApiAuthor> authors = new HashSet<>();
    private AcademicApiPaperExtended extendedProperties;

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setExtendedProperties(AcademicApiPaperExtended extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    public void addAuthor(AcademicApiAuthor author) {
        if (this.authors == null) {
            this.authors = new HashSet<>();
        }

        this.authors.add(author);
    }

    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new HashSet<>();
        }

        this.keywords.add(keyword);
    }

    public void addReference(Long reference) {
        if (this.references == null) {
            this.references = new HashSet<>();
        }

        this.references.add(reference);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public Set<AcademicApiAuthor> getAuthors() {
        return authors;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public Set<Long> getReferences() {
        return references;
    }

    public AcademicApiPaperExtended getExtendedProperties() {
        return extendedProperties;
    }

    public Integer getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(Integer citationCount) {
        this.citationCount = citationCount;
    }

    @Override
    public String toString() {
        StringBuilder stringValue = new StringBuilder();
        stringValue.append("id: ").append(this.id).append(", title: ").append(this.title);
        stringValue.append(", authors: ").append(this.authors.toString());
        stringValue.append(", refs: ").append(this.references.toString());

        return stringValue.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcademicApiPaper that = (AcademicApiPaper) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
