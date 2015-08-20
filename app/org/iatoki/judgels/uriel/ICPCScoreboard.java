package org.iatoki.judgels.uriel;

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
