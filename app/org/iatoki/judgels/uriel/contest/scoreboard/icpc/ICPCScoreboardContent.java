package org.iatoki.judgels.uriel.contest.scoreboard.icpc;

import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;

import java.util.List;

public final class ICPCScoreboardContent implements ScoreboardContent {

    private final List<ICPCScoreboardEntry> entries;

    public ICPCScoreboardContent(List<ICPCScoreboardEntry> entries) {
        this.entries = entries;
    }

    public List<ICPCScoreboardEntry> getEntries() {
        return entries;
    }
}
