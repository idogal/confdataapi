package com.idog.confdata.resources;

import com.idog.confdata.api.ExpandService;
import com.idog.confdata.app.ApiCache;
import com.idog.confdata.app.DiResources;
import com.idog.confdata.app.VisServerAppResources;
import org.xml.sax.InputSource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Path("admin")
public class AdminResource {

    @Path("source_papers")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInputPapers() throws IOException, URISyntaxException {
        URI uri = this.getClass().getClassLoader().getResource("paper_sources.xml").toURI();
        java.nio.file.Path resourcesFilePath = Paths.get(uri);
        List<String> lines = Files.readAllLines(resourcesFilePath);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(System.lineSeparator());
        }

        return Response.ok(sb.toString()).build();
    }

    @Path("source_papers")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_XML)
    public Response putInputPapers(String body) throws Exception {
        validateXml(body);
        URI uri = this.getClass().getClassLoader().getResource("paper_sources.xml").toURI();
        java.nio.file.Path resourcesFilePath = Paths.get(uri);
        File currentSourcesFile = resourcesFilePath.toFile();
        if (currentSourcesFile.exists()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String suffix = "_" + dateFormat.format(new Date()) + ".xml";
            String bkpFileName = resourcesFilePath.getFileName().toString().replace(".xml", suffix);
            java.nio.file.Path bkpFilePath = resourcesFilePath.resolveSibling(bkpFileName);
            Files.copy(resourcesFilePath, bkpFilePath);

            Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
            try (PrintStream out = new PrintStream(new FileOutputStream(resourcesFilePath.toFile()), true, charset.name())) {
                out.print(body);
            }
        }

        ApiCache apiCache = DiResources.getInjector().getInstance(VisServerAppResources.class).getApiCache();
        apiCache.clearAll();
        ExpandService.INSTANCE.clearTasks();

        return Response.ok("Updated paper sources").build();
    }

    public static void validateXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        builder.parse(is);
    }
}
