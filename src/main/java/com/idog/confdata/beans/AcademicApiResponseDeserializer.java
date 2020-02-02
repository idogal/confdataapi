package com.idog.confdata.beans;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AcademicApiResponseDeserializer extends StdDeserializer<AcademicApiResponse> {

    /**
     *
     */
    private static final long serialVersionUID = 5438031722273534623L;
    private static final Logger LOGGER = LogManager.getLogger(AcademicApiResponseDeserializer.class);

    public AcademicApiResponseDeserializer() {
        this(null);
    }

    public AcademicApiResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AcademicApiResponse deserialize(JsonParser jp, DeserializationContext dc) {
        AcademicApiResponse response = new AcademicApiResponse();
        String expr = "";
        try {
            JsonNode responseNode = null;
            try {
                responseNode = jp.getCodec().readTree(jp);
            } catch (IOException ex) {
                LOGGER.error(ex);
                return response;
            }

            // Response
            expr = responseNode.get("expr").textValue();
            response.expr = expr;
            LOGGER.debug("Serialising call for {}", expr);

            JsonNode entitiesNode = responseNode.get("entities");
            response.entities = new ArrayList<>();

            // Paper Entities
            Iterator<JsonNode> paperElements = entitiesNode.elements();
            while (paperElements.hasNext()) {
                AcademicApiPaper paper = new AcademicApiPaper();

                // Paper
                JsonNode paperNode = paperElements.next();
                if (paperNode == null) {
                    continue;
                }
                JsonNode tiNode = paperNode.get("Ti");
                JsonNode idNode = paperNode.get("Id");
                JsonNode yearNode = paperNode.get("Y");

                Long paperId = (idNode != null) ? idNode.asLong() : null;

                paper.setTitle((tiNode != null) ? tiNode.asText() : null);
                paper.setId(paperId);
                paper.setYear((yearNode != null) ? yearNode.asText() : null);

                LOGGER.debug("Title value: {}", (tiNode != null) ? tiNode.asText("") : "");
                LOGGER.debug("Id value: {}", paperId);
                LOGGER.debug("Year value: {}", (yearNode != null) ? yearNode.asText("") : "");

                JsonNode refNodes = paperNode.get("RId");
                if (refNodes != null && refNodes.isArray()) {
                    for (JsonNode refNode : refNodes) {
                        paper.addReference(refNode.asLong());
                        LOGGER.debug("Added reference for: {}", refNode.asLong());
                    }
                }

                JsonNode keywordNodes = paperNode.get("W");
                if (keywordNodes != null && keywordNodes.isArray()) {
                    for (JsonNode keywordNode : keywordNodes) {
                        paper.addKeyword(keywordNode.asText());
                        LOGGER.debug("Added keyword: {}", keywordNode.asText());
                    }
                }

                //////////////////////////
                // Composited Properties //
                //////////////////////////
                ObjectMapper mapper = new ObjectMapper(); // Check if it's possible to use the same ObjectMapper
                                                          // instance in the AppResources
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Authos
                JsonNode authorNodes = paperNode.get("AA");
                LOGGER.debug("Getting authors...");
                if (authorNodes != null && authorNodes.isArray()) {
                    for (JsonNode authorNode : authorNodes) {
                        JsonNode authorNameNode = authorNode.get("AuN");
                        JsonNode authorIdNode = authorNode.get("AuId");
                        JsonNode affNameNode = authorNode.get("AfN");
                        JsonNode affIdNode = authorNode.get("AfId");
                        JsonNode authorOrderNode = authorNode.get("S");

                        String authorName = (authorNameNode != null) ? authorNameNode.asText() : null;
                        Long authorId = (authorIdNode != null) ? authorIdNode.asLong() : null;
                        String affName = (affNameNode != null) ? affNameNode.asText() : null;
                        Long affId = (affIdNode != null) ? affIdNode.asLong() : null;
                        Integer order = (authorOrderNode != null) ? authorOrderNode.asInt() : null;

                        paper.addAuthor(new AcademicApiAuthor(paperId, authorName, authorId, affName, affId, order));
                        LOGGER.debug("Added author: {},{}", authorName, authorId);
                    }
                }

                // Extended, by deserialising again
                JsonNode extendedNode = paperNode.get("E");
                if (extendedNode == null) {
                    response.entities.add(paper);
                    LOGGER.debug("No Extended Properites...");
                    continue;
                }
                String extendedString = extendedNode.asText();
                AcademicApiPaperExtended paperExtended;
                try {
                    paperExtended = mapper.readValue(extendedString, AcademicApiPaperExtended.class);
                    LOGGER.debug("Added Extended Properites...");
                } catch (IOException ex) {
                    LOGGER.error(ex);
                    continue;
                }
                paper.setExtendedProperties(paperExtended);

                response.entities.add(paper);
            }
        } catch (NullPointerException e) {
            LOGGER.error(e);
        }

        LOGGER.info("Finished serialisation of {} succesfully", expr);
        return response;
    }

}
