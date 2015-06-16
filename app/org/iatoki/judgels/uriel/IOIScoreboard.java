package org.iatoki.judgels.uriel;

public class IOIScoreboard implements Scoreboard {
    private final ContestScoreState config;
    private final IOIScoreboardContent content;

    public IOIScoreboard(ContestScoreState config, IOIScoreboardContent content) {
        this.config = config;
        this.content = content;
    }

    @Override
    public ContestScoreState getState() {
        return config;
    }

    @Override
    public IOIScoreboardContent getContent() {
        return content;
    }
}
