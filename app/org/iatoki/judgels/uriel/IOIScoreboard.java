package org.iatoki.judgels.uriel;

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
