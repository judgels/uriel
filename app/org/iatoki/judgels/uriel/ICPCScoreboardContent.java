package org.iatoki.judgels.uriel;

import java.util.List;

public final class ICPCScoreboardContent implements ScoreboardContent {

    private final List<ICPCScoreboardEntry> entries;

    public ICPCScoreboardContent(List<ICPCScoreboardEntry> entries) {
        this.entries = entries;
    }

    public List<ICPCScoreboardEntry> getEntries() {
        return entries;
    }
}
