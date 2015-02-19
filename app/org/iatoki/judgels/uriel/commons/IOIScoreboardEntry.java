package org.iatoki.judgels.uriel.commons;

import java.util.Map;

public final class IOIScoreboardEntry implements ScoreboardEntry {
    private final String contestantJid;
    private final Map<String, Integer> scoresByProblemJid;
    private final int totalScores;

    public IOIScoreboardEntry(String contestantJid, Map<String, Integer> scoresByProblemJid, int totalScores) {
        this.contestantJid = contestantJid;
        this.scoresByProblemJid = scoresByProblemJid;
        this.totalScores = totalScores;
    }

    public String getContestantJid() {
        return contestantJid;
    }

    public Map<String, Integer> getScoresByProblemJid() {
        return scoresByProblemJid;
    }

    public int getTotalScores() {
        return totalScores;
    }
}
