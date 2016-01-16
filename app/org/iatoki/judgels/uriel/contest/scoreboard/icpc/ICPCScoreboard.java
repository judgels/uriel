package org.iatoki.judgels.uriel.contest.scoreboard.icpc;

import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;

public final class ICPCScoreboard implements Scoreboard {

    private ScoreboardState state;
    private ICPCScoreboardContent content;

    public ICPCScoreboard(ScoreboardState state, ICPCScoreboardContent content) {
        this.state = state;
        this.content = content;
    }

    @Override
    public ScoreboardState getState() {
        return state;
    }

    @Override
    public ScoreboardContent getContent() {
        return content;
    }
}
