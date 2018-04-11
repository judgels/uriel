package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;

public class IOIScoreboard implements Scoreboard {
    private final ScoreboardState state;

    private final IOIScoreboardContent content;

    public IOIScoreboard(ScoreboardState state, IOIScoreboardContent content) {
        this.state = state;
        this.content = content;
    }

    @Override
    public ScoreboardState getState() {
        return state;
    }

    @Override
    public IOIScoreboardContent getContent() {
        return content;
    }
}
