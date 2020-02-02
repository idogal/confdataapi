package com.idog.confdata.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.idog.confdata.app.DiResources;
import com.idog.confdata.app.VisServerAppResources;
import com.idog.confdata.beans.AcademicApiPaper;
import com.idog.confdata.beans.AcademicApiResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

public class VisMsApiService {

    private static final Logger LOGGER = LogManager.getLogger("VisApi");
    private final String MS_COGNITIVE_API_TARGET = "https://api.labs.cognitive.microsoft.com";
    private final String ACEDEMIC_API_EVALUATE_PATH = "academic/v1.0/evaluate";
    private final String ACADEMIC_API_SUBSCRIPTION_KEY = "46c107a906594111a8d94d822d2ef3be";

    private VisServerAppResources visServerAppResources;

    public List<AcademicApiPaper> getChasePapers() throws IOException {
        
        visServerAppResources = DiResources.getInjector().getInstance(VisServerAppResources.class);

        if (visServerAppResources == null)
            throw new RuntimeException("cant inject VisServerAppResources");
            
        List<PaperBasicInfo> listAllPapersToHandle = listAllPapersToHandle();
        List<AcademicApiPaper> apiPapers = getPapersDetails(listAllPapersToHandle);

        // // PERFORM THIS TASK IN A PARRALEL MANNER
        // List<AcademicApiPaper> apiPapers = listAllPapersToHandle.stream()
        //         .map(paperInfo -> getChasePaperById(paperInfo.getId(), false)).flatMap(List::stream)
        //         .collect(Collectors.toList());

        return apiPapers;
    }

    private List<PaperBasicInfo> listAllPapersToHandle() {
        ObjectMapper objectMapper = new XmlMapper();

        try {
            java.nio.file.Path resourcesFilePath = Paths
                    .get("C:\\Users\\idoga\\Documents\\Dev\\confdata\\src\\main\\resources\\paper_sources.xml");
            PapersBasicInfo papersInfo = objectMapper.readValue(Files.readAllBytes(resourcesFilePath),
                    PapersBasicInfo.class);
            LOGGER.info(papersInfo.getPapers().size());
            return papersInfo.getPapers();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AcademicApiPaper> getPapersDetails(List<PaperBasicInfo> chasePapersIds) {
        LOGGER.info("Trying to get the full details of the input papers list");
        List<AcademicApiPaper> allPapers = new ArrayList<>();
        int startFrom = 0;
        int batchSize = 10;
        boolean papersRemaining = true;
        while (papersRemaining) {
            int currentFinishPosition = startFrom + batchSize - 1;
            if (currentFinishPosition >= chasePapersIds.size() - 1) {
                papersRemaining = false;
                currentFinishPosition = chasePapersIds.size() - 1;
            }
            // Process current batch
            int poolSize = currentFinishPosition - startFrom + 1;
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);
            List<Future<List<AcademicApiPaper>>> tasks = new ArrayList<>();
            for (int i = startFrom; i <= currentFinishPosition; i++) {
                PaperBasicInfo chasePaper = chasePapersIds.get(i);
                // Create thread
                Future<List<AcademicApiPaper>> getPapersTask = executor.submit(() -> {
                    int attempts = 0;
                    for (int j = 0; j < 10; j++) {
                        String id = chasePaper .getId();
                        TimeUnit.MILLISECONDS.sleep((j + 1) * 200);
                        attempts = j;
                        return getChasePaperById(String.valueOf(id));
                    }
                    LOGGER.error("Could not get response after {} attempts", attempts);
                    return null;
                });
                tasks.add(getPapersTask);
            }

            // Read from current batch
            for (Future<List<AcademicApiPaper>> task : tasks) {
                try {
                    List<AcademicApiPaper> papersFromThread = task.get();
                    if (papersFromThread != null) {
                        allPapers.addAll(papersFromThread);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }

            try {
                executor.shutdown();
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                LOGGER.error(ex);
            } finally {
                if (!executor.isTerminated()) {
                    LOGGER.error("cancel non-finished tasks");
                }
                executor.shutdownNow();
            }
            startFrom = startFrom + batchSize;
        }
        LOGGER.info("Got the details of {} papers", allPapers.size());
        return allPapers;
    }    

    private List<AcademicApiPaper> getChasePaperById(String id) {
        LOGGER.info("Building a request by an ID for: {}", id);
        AcademicApiResponse readValue;

        // Send a query to the API if there is no cache
        String expr = "Id=" + id;
        String attributes = "Ti,Id,Y,E,D,CC,ECC,W,AA.AuN,AA.AuId,AA.AfN,AA.AfId,AA.S,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId";
        List<AbstractMap.SimpleEntry<String, Object>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<>("expr", expr));
        params.add(new AbstractMap.SimpleEntry<>("attributes", attributes));

        String entityJson = queryTheAcademicApi(params);
        try {
            readValue = this.visServerAppResources.getMapper().readValue(entityJson, AcademicApiResponse.class);
        } catch (IOException ex) {
            LOGGER.error(ex);
            return Collections.emptyList();
        }

        LOGGER.debug("Response was serialised into an AcademicApiResponse successfully");
        if (readValue.entities.isEmpty()) {
            LOGGER.warn("{} papers were found with '{}' id", readValue.entities.size(), id);
        } else {
            LOGGER.debug("{} papers were found with '{}' id", readValue.entities.size(), id);
        }

        return readValue.entities;
    }

    private String queryTheAcademicApi(List<AbstractMap.SimpleEntry<String, Object>> params)
            throws WebApplicationException {
        return queryTheAcademicApi(MS_COGNITIVE_API_TARGET, ACEDEMIC_API_EVALUATE_PATH, params);
    }

    private String queryTheAcademicApi(String webTarget, String webPath,
            List<AbstractMap.SimpleEntry<String, Object>> params) throws WebApplicationException {
        LOGGER.debug("Sending a query to the MS Academic API.");
        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget target = client.target(webTarget);
        target = target.path(webPath);
        for (AbstractMap.SimpleEntry<String, Object> param : params) {
            target = target.queryParam(param.getKey(), param.getValue().toString());
        }
        target = target.queryParam("model", "latest");
        LOGGER.debug("Target: {}, {}", target.toString(), params.toString());
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Ocp-Apim-Subscription-Key", ACADEMIC_API_SUBSCRIPTION_KEY);
        Response response = invocationBuilder.get();
        int status = response.getStatus();
        LOGGER.debug("Response status: {} - {}", String.valueOf(status), response.getStatusInfo().getReasonPhrase());
        if (status < 200 || status > 300) {
            String errorOutput = response.readEntity(String.class);
            if (status == 429) {
                LOGGER.warn(response.getStatusInfo().getReasonPhrase() + " - " + errorOutput);
                throw new WebApplicationException(response.getStatusInfo().getReasonPhrase());
            } else {
                LOGGER.error(response.getStatusInfo().getReasonPhrase() + " - " + errorOutput);
                throw new WebApplicationException(response.getStatusInfo().getReasonPhrase());
            }
        }
        LOGGER.debug("Entity was sucessfully retrieved from the API.");
        return response.readEntity(String.class);
    }

    // private String queryTheAcademicApi(String conferenceName, String year, int count) {
    //     String expr = "Composite(C.CN='" + conferenceName + "')";
    //     if (year != null && !year.isEmpty()) {
    //         expr = "And(" + expr + ", Y=" + year + ")";
    //     }
    //     List<AbstractMap.SimpleEntry<String, Object>> params = new ArrayList<>();
    //     params.add(new AbstractMap.SimpleEntry<>("expr", expr));
    //     params.add(new AbstractMap.SimpleEntry<>("attributes", "Ti,Id,Y,E"));
    //     params.add(new AbstractMap.SimpleEntry<>("Count", count));
    //     return queryTheAcademicApi(params);
    // }

    @JacksonXmlRootElement(localName = "Papers")
    private static class PapersBasicInfo {
        @JacksonXmlProperty(localName = "Paper")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<PaperBasicInfo> papers = new ArrayList<>();

        public List<PaperBasicInfo> getPapers() {
            return papers;
        }

        public void setPapers(List<PaperBasicInfo> papers) {
            this.papers = papers;
        }
    }

    @JacksonXmlRootElement(localName = "Paper")
    private static class PaperBasicInfo {

        @JacksonXmlProperty(localName = "ID")
        private String id;
        @JacksonXmlProperty(localName = "Conference_Year")
        private String year;
        @JacksonXmlProperty(localName = "Paper_Name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}