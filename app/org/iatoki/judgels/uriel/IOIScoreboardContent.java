package org.iatoki.judgels.uriel;

import java.util.List;

public final class IOIScoreboardContent implements ScoreboardContent {
    private final List<IOIScoreboardEntry> entries;

    public IOIScoreboardContent(List<IOIScoreboardEntry> entries) {
        this.entries = entries;
    }

    public List<IOIScoreboardEntry> getEntries() {
        return entries;
    }
}
