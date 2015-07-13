package org.iatoki.judgels.uriel;

public class IOIScoreboard implements Scoreboard {
    // If you want to fix this variable's name, you have to fix the scoreboard JSON data in database!
    private final ScoreboardState config;

    private final IOIScoreboardContent content;

    public IOIScoreboard(ScoreboardState state, IOIScoreboardContent content) {
        this.config = state;
        this.content = content;
    }

    @Override
    public ScoreboardState getState() {
        return config;
    }

    @Override
    public IOIScoreboardContent getContent() {
        return content;
    }
}
