
package com.idog.confdata.resources;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.app.*;
import com.idog.confdata.beans.AcademicAuthorPairCoupling;
import com.idog.confdata.beans.AcademicBibliographicCouplingItem;
import com.idog.confdata.beans.CouplingResult;
import com.idog.confdata.beans.CouplingResultType;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.api.AcademicApiPaper;
import com.idog.confdata.beans.responses.AbcEdge;
import com.idog.confdata.beans.responses.AbcNetwork;
import com.idog.confdata.beans.responses.AbcNode;
import com.idog.confdata.beans.responses.EdgeDirection;

@Path("")
public class AcademicDataResource {

    @Context
    private UriInfo uriInfo;

    VisMsApiService visMsApiService = new VisMsApiService();

    @Path("abc/network/edges/{resultNumber}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAuthorsBibliographicCouplingNetworkEdgesResults(@PathParam("resultNumber") Integer resultNumber) {
        if (resultNumber == null) {
            return null;
        }

        CouplingService couplingService = new CouplingService();
        CouplingResult results = couplingService.getQueuedAuthorBibliographicCouplingsResults(resultNumber);

        if (results.getCouplingResultType().equals(CouplingResultType.SUCCESS)) {
            List<AcademicBibliographicCouplingItem> couplings = results.getAcademicBibliographicCouplings();
            AbcNetwork network = buildNetwork(couplings);
            String s = formatEdgesAsCsv(network.getEdges());
            return Response.ok().entity(s).build();
        }

        if (results.getCouplingResultType().equals(CouplingResultType.FAILURE)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(results.getResultMessage()).build();
        }

        return Response.status(Response.Status.ACCEPTED).entity(results.getResultMessage()).build();
    }

    @Path("abc/network/nodes/{resultNumber}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAuthorsBibliographicCouplingNetworkNodesResults(@PathParam("resultNumber") Integer resultNumber) {
        if (resultNumber == null) {
            return null;
        }

        CouplingService couplingService = new CouplingService();
        CouplingResult results = couplingService.getQueuedAuthorBibliographicCouplingsResults(resultNumber);

        if (results.getCouplingResultType().equals(CouplingResultType.SUCCESS)) {
            List<AcademicBibliographicCouplingItem> couplings = results.getAcademicBibliographicCouplings();
            AbcNetwork network = buildNetwork(couplings);
            String s = formatNodesAsCsv(network.getNodes());
            return Response.ok().entity(s).build();
        }

        if (results.getCouplingResultType().equals(CouplingResultType.FAILURE)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(results.getResultMessage()).build();
        }

        return Response.status(Response.Status.ACCEPTED).entity(results.getResultMessage()).build();
    }

    @Path("abc/network/{resultNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorsBibliographicCouplingNetworkResults(@PathParam("resultNumber") Integer resultNumber) {
        if (resultNumber == null) {
            return null;
        }

        CouplingService couplingService = new CouplingService();
        CouplingResult results = couplingService.getQueuedAuthorBibliographicCouplingsResults(resultNumber);

        if (results.getCouplingResultType().equals(CouplingResultType.SUCCESS)) {
            List<AcademicBibliographicCouplingItem> couplings = results.getAcademicBibliographicCouplings();
            return Response.ok(buildNetwork(couplings)).build();
        }

        if (results.getCouplingResultType().equals(CouplingResultType.FAILURE)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(results.getResultMessage()).build();
        }

        return Response.status(Response.Status.ACCEPTED).entity(results.getResultMessage()).build();
    }

    private AbcNetwork buildNetwork(List<AcademicBibliographicCouplingItem> couplings) {
        Set<AbcEdge> edges = new HashSet<>();
        Set<AbcNode> nodes = new HashSet<>();

        int counter = 1;
        int x = 0;
        int y = 0;
        for (AcademicBibliographicCouplingItem c : couplings) {
            //int id = x + y;
            AcademicApiAuthor academicApiAuthorFirst = c.getAcademicApiAuthorFirst();
            String authorName = academicApiAuthorFirst.getAuthorName();
            nodes.add(new AbcNode(authorName, authorName, x, y, 1));

            x++;
            AcademicApiAuthor academicApiAuthorSecond = c.getAcademicApiAuthorSecond();
            String authorName2 = academicApiAuthorSecond.getAuthorName();
            nodes.add(new AbcNode(authorName2, authorName2, x, y, 1));

            y++;

            edges.add(new AbcEdge(String.valueOf(counter), c.getCouplingStrength(), authorName, authorName2, EdgeDirection.UNDIRECTED));
            counter++;
        }

        return new AbcNetwork(edges, nodes);
    }

    private String formatEdgesAsCsv(Set<AbcEdge> edges) {
        String collect = edges.stream().map(AbcEdge::toString).sorted().collect(Collectors.joining(System.lineSeparator()));
        return "Source,Target,Weight,Type,Id" + System.lineSeparator() + collect;
    }

    private String formatNodesAsCsv(Set<AbcNode> nodes) {
        String collect = nodes.stream().map(AbcNode::toString).sorted().collect(Collectors.joining(System.lineSeparator()));
        return "Id" + System.lineSeparator() + collect;
    }

    @Path("abc")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response queueAuthorsBibliographicCoupling(@QueryParam("year_start") String yearStart, @QueryParam("year_end") String yearEnd) {
        CouplingService couplingService = new CouplingService();
        int i = couplingService.queuePreparation(yearStart, yearEnd);

        UriBuilder resultPath = uriInfo.getAbsolutePathBuilder().path("network").path(Integer.toString(i));
        UriBuilder resultPathEdges = uriInfo.getAbsolutePathBuilder().path("network").path("edges").path(Integer.toString(i));
        UriBuilder resultPathNodes = uriInfo.getAbsolutePathBuilder().path("network").path("nodes").path(Integer.toString(i));
        String msg = String.format("Get results from:\n%s\n%s\n%s",
                resultPath.toTemplate(),
                resultPathEdges.toTemplate(),
                resultPathNodes.toTemplate());

        return Response
                .status(Response.Status.ACCEPTED)
                .entity(msg)
                .location(resultPath.build()).build();
    }

    @Path("abc/{resultNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorsBibliographicCouplingResult(@PathParam("resultNumber") Integer resultNumber) {
        if (resultNumber == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Please define a resultNumber").build();
        }

        CouplingService couplingService = new CouplingService();
        CouplingResult results = couplingService.getQueuedAuthorBibliographicCouplingsResults(resultNumber);

        if (results.getCouplingResultType().equals(CouplingResultType.SUCCESS)) {
            List<AcademicBibliographicCouplingItem> couplings = results.getAcademicBibliographicCouplings();

            return Response.ok(
                    couplings.stream()
                            .sorted(Comparator.comparing(AcademicBibliographicCouplingItem::getAcademicApiAuthorFirst)
                                    .thenComparing(AcademicBibliographicCouplingItem::getAcademicApiAuthorSecond))
                            .collect(Collectors.toList())).build();
        }

        if (results.getCouplingResultType().equals(CouplingResultType.FAILURE)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(results.getResultMessage()).build();
        }

        return Response.status(Response.Status.ACCEPTED).entity(results.getResultMessage()).build();
    }

    @Path("simple")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AcademicAuthorPairCoupling> getSimple() {
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
    public Response getAuthors(@QueryParam("sorted") boolean sorted,
                               @QueryParam("year_start") String yearStart,
                               @QueryParam("year_end") String yearEnd) {

        Collection<AcademicApiAuthor> authors = visMsApiService.deriveChaseAuthors(visMsApiService.getChasePapers(yearStart, yearEnd));

        if (sorted) {
            ArrayList<AcademicApiAuthor> authorArrayList = new ArrayList<>(authors);
            return Response.ok().entity(authorArrayList.stream().sorted()).build();
        }

        return Response.ok().entity(authors).build();
    }

    @Path("papers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChasePapers(@QueryParam("format") String format, @QueryParam("year_start") String yearStart, @QueryParam("year_end") String yearEnd) {
        List<AcademicApiPaper> academicApiPaper = visMsApiService.getChasePapers(yearStart, yearEnd);

        if (format != null && format.equalsIgnoreCase("csv")) {
            CsvMapper mapper = new CsvMapper();
            mapper.writerWithSchemaFor(AcademicApiPaper.class);
        }

        return Response.ok().entity(academicApiPaper).build();
    }

    @Path("authors/count")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAuthorsCount() {
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