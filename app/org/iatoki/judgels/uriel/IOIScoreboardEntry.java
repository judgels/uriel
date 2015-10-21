package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;

import java.net.URL;
import java.util.List;

public final class IOIScoreboardEntry implements ScoreboardEntry, Comparable<IOIScoreboardEntry> {

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public List<Integer> scores;
    public int totalScores;

    public IOIScoreboardEntry() {
        scores = Lists.newArrayList();
    }

    @Override
    public int compareTo(IOIScoreboardEntry o) {
        int ignoringTieBreaker = compareToIgnoringTieBreakerForEqualRanks(o);

        if (ignoringTieBreaker != 0) {
            return ignoringTieBreaker;
        }

        return compareToUsingTieBreakerForEqualRanks(o);
    }

    public int compareToIgnoringTieBreakerForEqualRanks(IOIScoreboardEntry o) {
        return Integer.compare(o.totalScores, totalScores);
    }

    private int compareToUsingTieBreakerForEqualRanks(IOIScoreboardEntry o) {
        int submittedProblems = (int) scores.stream().filter(s -> s != null).count();
        int otherSubmittedProblems = (int) o.scores.stream().filter(s -> s != null).count();

        // prioritize entry which has more number of problems that have at least one submission
        if (submittedProblems != otherSubmittedProblems) {
            return Integer.compare(otherSubmittedProblems, submittedProblems);
        }

        return contestantJid.compareTo(o.contestantJid);
    }
}
