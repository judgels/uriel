package org.iatoki.judgels.uriel;

import org.iatoki.judgels.uriel.commons.Scoreboard;

public final class ContestScoreboard {
    private final long id;
    private final String contestJid;
    private final ContestScoreboardType type;
    private final Scoreboard scoreboard;

    public ContestScoreboard(long id, String contestJid, ContestScoreboardType type, Scoreboard scoreboard) {
        this.id = id;
        this.contestJid = contestJid;
        this.type = type;
        this.scoreboard = scoreboard;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public ContestScoreboardType getType() {
        return type;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
