
package com.idog.confdata.resources;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.beans.AcademicApiAuthor;
import com.idog.confdata.beans.AcademicApiPaper;

import javax.ws.rs.Produces;

@Path("academicdata")
public class AcademicDataResource {

    @Path("authors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthors() throws IOException {
        List<AcademicApiPaper> academicApiPaper = new VisMsApiService().getChasePapers();
        List<AcademicApiAuthor> authors = academicApiPaper.stream()
            .map(paper -> paper.getAuthors()).flatMap(List::stream)
            .collect(Collectors.toList());

        return Response.ok().entity(authors).build();

        // ObjectMapper m = visServerAppResources.getMapper();
        // return Response.ok().entity(m).build();
    }

    @Path("papers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChasePapers() {
        try {
            final List<AcademicApiPaper> academicApiPaper = new VisMsApiService().getChasePapers();
            return Response.ok().entity(academicApiPaper).build();
        } catch (final IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

}