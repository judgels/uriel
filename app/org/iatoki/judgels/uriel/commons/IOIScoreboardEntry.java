package org.iatoki.judgels.uriel.commons;

import java.util.List;

public final class IOIScoreboardEntry implements ScoreboardEntry, Comparable<IOIScoreboardEntry> {
    public int rank;
    public String contestantJid;
    public List<Integer> scores;
    public int totalScores;

    @Override
    public int compareTo(IOIScoreboardEntry o) {
        if (totalScores != o.totalScores) {
            return o.totalScores - totalScores;
        }

        return contestantJid.compareTo(o.contestantJid);
    }
}
