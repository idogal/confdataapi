package com.idog.confdata.beans;

import java.util.List;

public class CouplingResult {
    private final CouplingResultType couplingResultType;
    private final String resultMessage;
    private final List<AcademicBibliographicCouplingItem> academicBibliographicCouplings;

    private CouplingResult(CouplingResultType couplingResultType, String resultMessage, List<AcademicBibliographicCouplingItem> academicBibliographicCouplings) {
        this.couplingResultType = couplingResultType;
        this.resultMessage = resultMessage;
        this.academicBibliographicCouplings = academicBibliographicCouplings;
    }

    public CouplingResultType getCouplingResultType() {
        return couplingResultType;
    }

    public List<AcademicBibliographicCouplingItem> getAcademicBibliographicCouplings() {
        return academicBibliographicCouplings;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public static CouplingResultBuilder builder() {
        return new CouplingResultBuilder();
    }

    public static class CouplingResultBuilder {

        private CouplingResultType couplingResultType;
        private String resultMessage;
        private List<AcademicBibliographicCouplingItem> academicBibliographicCouplings;

        public CouplingResultBuilder setResultType(CouplingResultType couplingResultType) {
            this.couplingResultType = couplingResultType;
            return this;
        }

        public CouplingResultBuilder setMessage(String resultMessage) {
            this.resultMessage = resultMessage;
            return this;
        }

        public CouplingResultBuilder setCouplings(List<AcademicBibliographicCouplingItem> academicBibliographicCouplings) {
            this.academicBibliographicCouplings = academicBibliographicCouplings;
            return this;
        }

        public CouplingResult build() {
            return new CouplingResult(couplingResultType, resultMessage, academicBibliographicCouplings);
        }
    }
}
