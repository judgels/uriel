package org.iatoki.judgels.uriel.contest.scoreboard.icpc;

import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;

public final class OldICPCScoreboard implements Scoreboard {

    private ScoreboardState state;
    private OldICPCScoreboardContent content;

    public OldICPCScoreboard(ScoreboardState state, OldICPCScoreboardContent content) {
        this.state = state;
        this.content = content;
    }

    @Override
    public ScoreboardState getState() {
        return state;
    }

    @Override
    public OldICPCScoreboardContent getContent() {
        return content;
    }
}
