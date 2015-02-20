package org.iatoki.judgels.uriel.commons;

import java.util.Map;

public final class IOIScoreboardEntry implements ScoreboardEntry, Comparable<IOIScoreboardEntry> {
    public int rank;
    public String contestantJid;
    public Map<String, Integer> scoresByProblemJid;
    public int totalScores;

    @Override
    public int compareTo(IOIScoreboardEntry o) {
        if (totalScores != o.totalScores) {
            return o.totalScores - totalScores;
        }

        return contestantJid.compareTo(o.contestantJid);
    }
}
