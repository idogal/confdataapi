
package com.idog.confdata.resources;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.idog.confdata.api.ExpandResult;
import com.idog.confdata.api.ExpandService;
import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.app.CouplingService;
import com.idog.confdata.app.CouplingServiceUtils;
import com.idog.confdata.app.DataNotYetReadyException;
import com.idog.confdata.beans.AcademicAuthorPairCoupling;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;
import com.idog.confdata.beans.responses.AbcEdge;
import com.idog.confdata.beans.responses.AbcNetwork;
import com.idog.confdata.beans.responses.AbcNode;

import javax.ws.rs.Produces;
import javax.ws.rs.core.UriInfo;

@Path("academicdata")
public class AcademicDataResource {

    @Context
    private UriInfo uriInfo;

    VisMsApiService visMsApiService = new VisMsApiService();

    @Path("abc/network")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorsBibliographicCouplingNetwork() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();
        CouplingService couplingService = new CouplingService();
        List<AcademicBibliographicCouplingItem> couplings;
        try {
            couplings = couplingService.getAuthorBibliographicCouplingsResults(academicApiPapers, authors);
        } catch (DataNotYetReadyException e) {
            return Response
                    .temporaryRedirect(uriInfo.getBaseUri().resolve("/papers/expand/status"))
                    .entity(e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

        Set<AbcEdge> edges = new HashSet<>();
        Set<AbcNode> nodes = new HashSet<>();

        int counter = 0;
        int x = 0;
        int y = 0;
        for (AcademicBibliographicCouplingItem c : couplings) {
            int id = x + y;
            AcademicApiAuthor academicApiAuthorFirst = c.getAcademicApiAuthorFirst();
            String authorName = academicApiAuthorFirst.getAuthorName();
            nodes.add(new AbcNode(authorName, authorName, x, y, 1));

            x++;
            AcademicApiAuthor academicApiAuthorSecond = c.getAcademicApiAuthorSecond();
            String authorName2 = academicApiAuthorSecond.getAuthorName();
            nodes.add(new AbcNode(authorName2, authorName2, x, y, 1));

            y++;

            edges.add(new AbcEdge(String.valueOf(id), authorName, authorName2));
        }

        return Response.ok(new AbcNetwork(edges, nodes)).build();
    }

    @Path("abc")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorsBibliographicCoupling() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();
        CouplingService couplingService = new CouplingService();
        List<AcademicBibliographicCouplingItem> couplings;
        try {
            couplings = couplingService.getAuthorBibliographicCouplingsResults(academicApiPapers, authors);
        } catch (DataNotYetReadyException e) {
            return Response
                    .temporaryRedirect(uriInfo.getBaseUri().resolve("/papers/expand/status"))
                    .entity(e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return Response.ok(
                couplings.stream()
                        .sorted(Comparator.comparing(AcademicBibliographicCouplingItem::getAcademicApiAuthorFirst)
                                .thenComparing(AcademicBibliographicCouplingItem::getAcademicApiAuthorSecond))
                        .collect(Collectors.toList())).build();
    }

    @Path("simple")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AcademicAuthorPairCoupling> getSimple() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();

        HashMap<String, Set<String>> doneAuthors = new HashMap<>();

        HashMap<AcademicApiAuthor, Set<AcademicApiPaper>> papersPerAuthor = new HashMap<>();
        academicApiPapers.forEach(p ->
                p.getAuthors().forEach(a -> {
                    Set<AcademicApiPaper> papersForCurrentAuthor = papersPerAuthor.getOrDefault(a, new HashSet<>());
                    papersForCurrentAuthor.add(p);
                    papersPerAuthor.putIfAbsent(a, papersForCurrentAuthor);
                })
        );

        List<AcademicAuthorPairCoupling> couplings = new ArrayList<>();
        CouplingServiceUtils.getAuthorPairs(authors).forEach(pair -> {
            AcademicApiAuthor academicApiAuthorFirst = pair.getAcademicApiAuthorFirst();
            AcademicApiAuthor academicApiAuthorSecond = pair.getAcademicApiAuthorSecond();

            String firstName = academicApiAuthorFirst.getAuthorName();
            String secondName = academicApiAuthorSecond.getAuthorName();
            Set<String> couplingsOfSecond = doneAuthors.getOrDefault(secondName, new HashSet<>());
            if (couplingsOfSecond.contains(firstName)) {
                return;
            }
            couplingsOfSecond.add(secondName);
            doneAuthors.putIfAbsent(firstName, couplingsOfSecond);

            Set<Long> refsOfAuthor = papersPerAuthor.get(academicApiAuthorFirst).stream()
                    .map(AcademicApiPaper::getReferences)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            Set<AcademicApiPaper> collect = academicApiPapers.stream()
                    .filter(paper -> refsOfAuthor.contains(paper.getId())) // All papers cited by A
                    .filter(paper -> paper.getAuthors().contains(academicApiAuthorSecond)) //All papers that contain author B
                    .collect(Collectors.toSet());

            int coupling = collect.size();
            if (coupling > 0) {
                couplings.add(new AcademicAuthorPairCoupling(pair, coupling, collect));
            }
        });

        //String collect = couplings.stream().sorted().map(AcademicAuthorPairCoupling::toString).collect(Collectors.joining(System.lineSeparator()));
        return couplings.stream().sorted().collect(Collectors.toList());
    }

    @Path("authors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthors(@QueryParam("sorted") boolean sorted) throws IOException {
        Collection<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();

        if (sorted) {
            ArrayList<AcademicApiAuthor> authorArrayList = new ArrayList<>(authors);
            return Response.ok().entity(authorArrayList.stream().sorted()).build();
        }

        return Response.ok().entity(authors).build();
    }

    @Path("papers/expand")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response expandPapers() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();
        ExpandService expandService = ExpandService.INSTANCE;
        List<Future<ExpandResult>> futures = expandService.expandAsync(academicApiPapers, authors);
        return Response.ok("submitted [" + futures.size() + "]").build();
    }

    @Path("papers/expand/status")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response expandPapersStatus() {
        ExpandService expandService = ExpandService.INSTANCE;
        long l = expandService.checkStatus();
        int size = expandService.getTasks().size();
        return Response.ok(String.format("done = [%s], total = [%s]", l, size)).build();
    }

    @Path("papers/expand/cancel")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response cancelExpandPapers() {
        ExpandService expandService = ExpandService.INSTANCE;
        expandService.cancelAll();
        return Response.accepted("Cancelled all").build();
    }

    @Path("papers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChasePapers() {
        final List<AcademicApiPaper> academicApiPaper = visMsApiService.getChasePapers();
        return Response.ok().entity(academicApiPaper).build();
    }

    @Path("authors/count")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAuthorsCount() throws IOException {
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();
        return Response.ok().entity(authors.size()).build();
    }

    @Path("papers/count")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getChasePapersCount() {
        final List<AcademicApiPaper> academicApiPaper = visMsApiService.getChasePapers();
        return Response.ok().entity(academicApiPaper.size()).build();
    }

}