package org.iatoki.judgels.uriel.contest.scoreboard.icpc;

import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;

import java.util.List;

public final class OldICPCScoreboardContent implements ScoreboardContent {

    private final List<OldICPCScoreboardEntry> entries;

    public OldICPCScoreboardContent(List<OldICPCScoreboardEntry> entries) {
        this.entries = entries;
    }

    public List<OldICPCScoreboardEntry> getEntries() {
        return entries;
    }
}
