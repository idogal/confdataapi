package com.idog.confdata.beans.api;

public class AcademicApiPaperAuthor {
    private Long paperId;
    private AcademicApiAuthor academicApiAuthor;
    
    public AcademicApiPaperAuthor(Long paperId, AcademicApiAuthor academicApiAuthor) {
        this.paperId = paperId;
        this.academicApiAuthor = academicApiAuthor;
    }
}
