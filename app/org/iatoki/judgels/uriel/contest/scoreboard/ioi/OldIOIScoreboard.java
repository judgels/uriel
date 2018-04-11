package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;

public class OldIOIScoreboard implements Scoreboard {
    // If you want to fix this variable's name, you have to fix the scoreboard JSON data in database!
    private final ScoreboardState config;

    private final OldIOIScoreboardContent content;

    public OldIOIScoreboard(ScoreboardState config, OldIOIScoreboardContent content) {
        this.config = config;
        this.content = content;
    }

    @Override
    public ScoreboardState getState() {
        return config;
    }

    @Override
    public OldIOIScoreboardContent getContent() {
        return content;
    }
}
