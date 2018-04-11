package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;

import java.util.List;

public final class OldIOIScoreboardContent implements ScoreboardContent {

    private final List<OldIOIScoreboardEntry> entries;

    public OldIOIScoreboardContent(List<OldIOIScoreboardEntry> entries) {
        this.entries = entries;
    }

    public List<OldIOIScoreboardEntry> getEntries() {
        return entries;
    }
}
