package com.idog.confdata.app;

import com.google.common.collect.Sets;
import com.idog.confdata.beans.api.AcademicApiAuthor;
import com.idog.confdata.beans.AcademicAuthorPair;

import java.util.*;

public class CouplingService {

    public Set<AcademicAuthorPair> getAuthorPairs(Set<AcademicApiAuthor> authors) {
        Set<AcademicAuthorPair> newAuthors = new HashSet<>();

        Set<List<AcademicApiAuthor>> sets = Sets.cartesianProduct(authors, authors);
        Iterator<List<AcademicApiAuthor>> iterator = sets.iterator();
        while (iterator.hasNext()) {
            List<AcademicApiAuthor> tuple =  iterator.next();
            AcademicApiAuthor firstElement = tuple.get(0);
            AcademicApiAuthor secondElement = tuple.get(1);

            if (!firstElement.getAuthorName().equals(secondElement.getAuthorName()))
                newAuthors.add(new AcademicAuthorPair(firstElement, secondElement));
        }

        return newAuthors;
    }
}
