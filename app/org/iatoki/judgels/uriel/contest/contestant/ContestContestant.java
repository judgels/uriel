package org.iatoki.judgels.uriel.contest.contestant;

import java.util.Date;

public final class ContestContestant {

    private final long id;
    private final String contestJid;
    private final String userJid;
    private final ContestContestantStatus status;
    private final Date contestStartTime;

    public ContestContestant(long id, String contestJid, String userJid, ContestContestantStatus status, Date contestStartTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.status = status;
        this.contestStartTime = contestStartTime;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getUserJid() {
        return userJid;
    }

    public ContestContestantStatus getStatus() {
        return status;
    }

    public long getContestStartTime() {
        return contestStartTime == null ? 0 : contestStartTime.getTime();
    }
}
