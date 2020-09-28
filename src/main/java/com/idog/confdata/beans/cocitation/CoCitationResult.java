package com.idog.confdata.beans.cocitation;

import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.CouplingResultType;

import java.util.List;
import java.util.Map;

public class CoCitationResult {
    private final CouplingResultType couplingResultType;
    private final String resultMessage;
    private final Map<CoCitationCouple, CoCitationScore> couplesToPapers;

    private CoCitationResult(CouplingResultType couplingResultType, String resultMessage, Map<CoCitationCouple, CoCitationScore> couplesToPapers) {
        this.couplingResultType = couplingResultType;
        this.resultMessage = resultMessage;
        this.couplesToPapers = couplesToPapers;
    }

    public CouplingResultType getCouplingResultType() {
        return couplingResultType;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public Map<CoCitationCouple, CoCitationScore> getCouplesToPapers() {
        return couplesToPapers;
    }

    public static CoCitationResultBuilder builder() {
        return new CoCitationResultBuilder();
    }

    public static class CoCitationResultBuilder {

        private CouplingResultType couplingResultType;
        private String resultMessage;
        private Map<CoCitationCouple, CoCitationScore> couplesToPapers;

        public CoCitationResultBuilder setResultType(CouplingResultType couplingResultType) {
            this.couplingResultType = couplingResultType;
            return this;
        }

        public CoCitationResultBuilder setMessage(String resultMessage) {
            this.resultMessage = resultMessage;
            return this;
        }

        public CoCitationResultBuilder setCouplings(Map<CoCitationCouple, CoCitationScore> couplesToPapers) {
            this.couplesToPapers = couplesToPapers;
            return this;
        }

        public CoCitationResult build() {
            return new CoCitationResult(couplingResultType, resultMessage, couplesToPapers);
        }
    }
}
