package org.iatoki.judgels.uriel.commons;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class IOIScoreboardEntry implements ScoreboardEntry, Comparable<IOIScoreboardEntry> {

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public List<Integer> scores;
    public int totalScores;

    public IOIScoreboardEntry() {
        scores = new ArrayList<>();
    }

    @Override
    public int compareTo(IOIScoreboardEntry o) {
        if (totalScores != o.totalScores) {
            return o.totalScores - totalScores;
        }

        int submittedProblems = (int) scores.stream().filter(s -> s != null).count();
        int otherSubmittedProblems = (int) o.scores.stream().filter(s -> s != null).count();

        // prioritize entry which has more number of problems that have at least one submission
        if (submittedProblems != otherSubmittedProblems) {
            return otherSubmittedProblems - submittedProblems;
        }

        return contestantJid.compareTo(o.contestantJid);
    }
}
