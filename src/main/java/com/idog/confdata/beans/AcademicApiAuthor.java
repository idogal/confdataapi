package com.idog.confdata.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcademicApiAuthor {

    private Long paperId;
    @JsonProperty("AuN") private String authorName;
    @JsonProperty("AuId") private Long authorId;
    @JsonProperty("AfN") private String affiliationName;
    @JsonProperty("AfId") private Long affiliationId;
    @JsonProperty("S") private Integer paperOrder;

    public AcademicApiAuthor(Long paperId, String authorName, Long authorId, String affiliationName, Long affiliationId, Integer paperOrder) {
        this.paperId = paperId;
        this.authorName = authorName;
        this.authorId = authorId;
        this.affiliationName = affiliationName;
        this.affiliationId = affiliationId;
        this.paperOrder = paperOrder;
    }    

    public Long getPaperId() {
        return paperId;
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

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }
}
