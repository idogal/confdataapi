package com.idog.confdata.beans;

import java.util.ArrayList;
import java.util.List;

public class AcademicApiPaper {

    private Long id;
    private String title;
    private String year;
    private List<Long> references = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
    private List<AcademicApiAuthor> authors = new ArrayList<>();
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
            this.authors = new ArrayList<>();
        }

        this.authors.add(author);
    }

    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new ArrayList<>();
        }

        this.keywords.add(keyword);
    }

    public void addReference(Long reference) {
        if (this.references == null) {
            this.references = new ArrayList<>();
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

    public List<AcademicApiAuthor> getAuthors() {
        return authors;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<Long> getReferences() {
        return references;
    }

    public AcademicApiPaperExtended getExtendedProperties() {
        return extendedProperties;
    }

    @Override
    public String toString() {
        StringBuilder stringValue = new StringBuilder();
        stringValue.append("id: ").append(this.id).append(", title: ").append(this.title);
        stringValue.append(", authors: ").append(this.authors.toString());
        stringValue.append(", refs: ").append(this.references.toString());

        return stringValue.toString();
    }
}
