package com.idog.confdata.beans.cocitation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoCitationScore {
    private List<String> docNames = new ArrayList<>();
    private List<Long> docIds = new ArrayList<>();
    private int score;

    private CoCitationScore() {
    }

    public int getScore() {
        return score;
    }

    List<Long> getDocIds() {
        return docIds;
    }

    List<String> getDocNames() {
        return docNames;
    }

    public static CoCitationScoreBuilder builder() {
        return new CoCitationScoreBuilder();
    }

    public static class CoCitationScoreBuilder {
        private List<String> docNames = new ArrayList<>();
        private List<Long> docIds = new ArrayList<>();
        private int score = 0;

        public CoCitationScoreBuilder addDoc(String docName, long docId) {
            this.docNames.add(docName);
            this.docIds.add(docId);
            score += 1;
            return this;
        }

        public CoCitationScore build() {
            CoCitationScore score = new CoCitationScore();
            score.docNames = this.docNames;
            score.docIds = this.docIds;
            score.score = this.score;

            return score;
        }

        @Override
        public String toString() {
            List<String> vals = new ArrayList<>();
            for (int i = 0; i < score; i++) {
                String name = docNames.get(i);
                Long id = docIds.get(i);
                vals.add(String.format("({} - {})", name, id));
            }

            return String.join(",", vals);
        }
    }
}
