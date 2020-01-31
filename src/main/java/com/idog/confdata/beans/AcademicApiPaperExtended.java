package com.idog.confdata.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcademicApiPaperExtended {

    @JsonProperty("DN")
    public String displayName = "";

    @JsonProperty("VFN")
    public String venueFullName = "";

    @JsonProperty("VSN")
    public String venueShortName = "";

    @JsonProperty("V")
    public String journalVolume = "";

    @JsonProperty("I")
    public String journalIssue = "";

    @JsonProperty("BV")
    public String bv = "";
    
    @JsonProperty("DOI")
    public String doi = "";
    
    public String getDisplayName() {
        return displayName;
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public String getVenueFullName() {
        return venueFullName;
    }

    public String getVenueShortName() {
        return venueShortName;
    }

    public String getBv() {
        return bv;
    }
}
