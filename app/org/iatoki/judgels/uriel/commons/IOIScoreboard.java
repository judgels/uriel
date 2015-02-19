package org.iatoki.judgels.uriel.commons;

public class IOIScoreboard implements Scoreboard {
    private final ContestConfig config;
    private final IOIScoreboardContent content;

    public IOIScoreboard(ContestConfig config, IOIScoreboardContent content) {
        this.config = config;
        this.content = content;
    }

    @Override
    public ContestConfig getConfig() {
        return config;
    }

    @Override
    public IOIScoreboardContent getContent() {
        return content;
    }
}
