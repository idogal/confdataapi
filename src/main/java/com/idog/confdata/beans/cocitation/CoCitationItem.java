package com.idog.confdata.beans.cocitation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CoCitationItem {
    @JsonProperty("firstDocumentName")
    private String firstDocumentName;
    @JsonProperty("secondDocumentName")
    private String secondDocumentName;
    @JsonProperty("firstDocumentId")
    private long firstDocumentId;
    @JsonProperty("secondDocumentId")
    private long secondDocumentId;
    @JsonProperty("co-citation strength")
    private int strength;
    @JsonProperty("source documents")
    private List<String> sources = new ArrayList<>();

    private CoCitationItem(String firstDocumentName, String secondDocumentName, long firstDocumentId, long secondDocumentId, int strength, List<String> sources) {
        this.firstDocumentName = firstDocumentName;
        this.secondDocumentName = secondDocumentName;
        this.firstDocumentId = firstDocumentId;
        this.secondDocumentId = secondDocumentId;
        this.strength = strength;
        this.sources = sources;
    }

    public String getFirstDocumentName() {
        return firstDocumentName;
    }

    public String getSecondDocumentName() {
        return secondDocumentName;
    }

    public long getFirstDocumentId() {
        return firstDocumentId;
    }

    public long getSecondDocumentId() {
        return secondDocumentId;
    }

    public int getStrength() {
        return strength;
    }

    public List<String> getSources() {
        return sources;
    }

    public static CoCitationItemBuilder builder() {
        return new CoCitationItemBuilder();
    }

    public static class CoCitationItemBuilder {
        private String firstDocumentName;
        private String secondDocumentName;
        private long firstDocumentId;
        private long secondDocumentId;
        private int strength;
        private List<String> sources = new ArrayList<>();

        public CoCitationItemBuilder addCoCitationCouple(CoCitationCouple couple) {
            this.firstDocumentName = couple.getPaperA().getTitle();
            this.secondDocumentName = couple.getPaperB().getTitle();
            this.firstDocumentId = couple.getPaperA().getId();
            this.secondDocumentId = couple.getPaperB().getId();

            return this;
        }

        public CoCitationItemBuilder addCoCitationScore(CoCitationScore score) {
            this.strength = score.getScore();
            this.sources = score.getDocNames();

            return this;
        }

        public CoCitationItem build() {
            return new CoCitationItem(this.firstDocumentName,
                    this.secondDocumentName,
                    this.firstDocumentId,
                    this.secondDocumentId,
                    this.strength,
                    this.sources);
        }


    }
}
