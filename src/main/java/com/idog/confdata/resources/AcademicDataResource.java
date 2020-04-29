
package com.idog.confdata.resources;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.idog.confdata.api.VisMsApiService;
import com.idog.confdata.app.CouplingService;
import com.idog.confdata.beans.AcademicAuthorPairCoupling;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.AcademicAuthorPair;
import com.idog.confdata.beans.api.AcademicApiPaper;

import javax.ws.rs.Produces;

@Path("academicdata")
public class AcademicDataResource {

    VisMsApiService visMsApiService = new VisMsApiService();

    @Path("abc")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorsBibliographicCoupling() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        HashMap<AcademicApiAuthor, Set<AcademicApiPaper>> papersPerAuthor = new HashMap<>();
        academicApiPapers.forEach(p ->
                p.getAuthors().forEach(a -> {
                    Set<AcademicApiPaper> papersForCurrentAuthor = papersPerAuthor.getOrDefault(a, new HashSet<>());
                    papersForCurrentAuthor.add(p);
                    papersPerAuthor.putIfAbsent(a, papersForCurrentAuthor);
                })
        );

        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();

        CouplingService couplingService = new CouplingService();

        Map<AcademicAuthorPair, Integer> couplings = new HashMap<>();
        couplingService.getAuthorPairs(authors).forEach(pair -> {
            AcademicApiAuthor academicApiAuthorFirst = pair.getAcademicApiAuthorFirst();
            AcademicApiAuthor academicApiAuthorSecond = pair.getAcademicApiAuthorSecond();

            Set<Long> refsOfA = papersPerAuthor.get(academicApiAuthorFirst).stream()
                    .filter(p -> !p.getAuthors().contains(academicApiAuthorSecond))
                    .map(AcademicApiPaper::getReferences).flatMap(Set::stream)
                    .collect(Collectors.toSet());

            Set<Long> refsOfB = papersPerAuthor.get(academicApiAuthorSecond).stream()
                    .filter(p -> !p.getAuthors().contains(academicApiAuthorFirst))
                    .map(AcademicApiPaper::getReferences).flatMap(Set::stream)
                    .collect(Collectors.toSet());

            List<AcademicApiPaper> paparsThatWereReferencedByA = visMsApiService.fetchById(refsOfA);
            List<AcademicApiPaper> paparsThatWereReferencedByB = visMsApiService.fetchById(refsOfB);


            Map<String, List<AcademicApiPaper>> a = paparsThatWereReferencedByA.stream().collect(Collectors.groupingBy(AcademicApiPaper::getTitle));
            Map<String, List<AcademicApiPaper>> b = paparsThatWereReferencedByB.stream().collect(Collectors.groupingBy(AcademicApiPaper::getTitle));

            List<Integer> weights = new ArrayList<>();
            a.forEach((nameOfA, fromA) -> {
                List<AcademicApiPaper> fromB = b.get(nameOfA);

                if (fromB == null) {
                    return;
                }

                int min = Math.min(fromA.size(), fromB.size());
                weights.add(min);
            });

            b.forEach((nameOfB, fromB) -> {
                List<AcademicApiPaper> fromA = a.get(nameOfB);

                if (fromA == null) {
                    return;
                }

                int min = Math.min(fromB.size(), fromA.size());
                weights.add(min);
            });

            int sum = weights.stream().mapToInt(i -> i).sum();
            if (sum > 0)
                couplings.put(pair, sum);
        });

        return Response.accepted().build();
    }

    @Path("simple")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AcademicAuthorPairCoupling> getSimple() throws IOException {
        List<AcademicApiPaper> academicApiPapers = visMsApiService.getChasePapers();
        Set<AcademicApiAuthor> authors = visMsApiService.getChaseAuthors();

        CouplingService couplingService = new CouplingService();
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
        couplingService.getAuthorPairs(authors).forEach(pair -> {
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
            authorArrayList.sort(Comparator.comparing(AcademicApiAuthor::getAuthorName));
            return Response.ok().entity(authorArrayList).build();
        }

        return Response.ok().entity(authors).build();
    }

//    @Path("papers/expand")
//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response expandPapers() {
//
//
//
//
//
//        return Response.accepted().build();
//    }

    @Path("papers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChasePapers() {
        try {
            final List<AcademicApiPaper> academicApiPaper = visMsApiService.getChasePapers();
            return Response.ok().entity(academicApiPaper).build();
        } catch (final IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
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
        try {
            final List<AcademicApiPaper> academicApiPaper = visMsApiService.getChasePapers();
            return Response.ok().entity(academicApiPaper.size()).build();
        } catch (final IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

}