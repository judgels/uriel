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

        return contestantJid.compareTo(o.contestantJid);
    }
}
