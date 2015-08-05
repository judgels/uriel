package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestScoreboard {
    private final long id;
    private final String contestJid;
    private final ContestScoreboardType type;
    private final Scoreboard scoreboard;
    private final Date lastUpdateTime;

    public ContestScoreboard(long id, String contestJid, ContestScoreboardType type, Scoreboard scoreboard, Date lastUpdateTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.type = type;
        this.scoreboard = scoreboard;
        this.lastUpdateTime = lastUpdateTime;
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

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
}
