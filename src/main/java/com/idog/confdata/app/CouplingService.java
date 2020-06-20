package com.idog.confdata.app;

import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

public class CouplingService {
    private static final Logger LOGGER = LogManager.getLogger(CouplingService.class);

    public List<AcademicBibliographicCouplingItem> getAuthorBibliographicCouplingsResults(List<AcademicApiPaper> papers, Set<AcademicApiAuthor> authors) throws DataNotYetReadyException {

        ApiCache apiCache = null;
        try {
            VisServerAppResources instance = DiResources.getInjector().getInstance(VisServerAppResources.class);
            apiCache = instance.getApiCache();
            List<AcademicBibliographicCouplingItem> abcCouplingResults = apiCache.getAbcCouplingResults();
            if (abcCouplingResults != null)
                return abcCouplingResults;

        } catch (Exception exception) {
            LOGGER.error(exception);
        }

        List<AcademicBibliographicCouplingItem> authorBibliographicCouplings = CouplingServiceUtils.getAuthorBibliographicCouplings(papers, authors);
        if (apiCache != null)
            apiCache.setAbcCouplingResults(authorBibliographicCouplings);

        return authorBibliographicCouplings;
    }
}
