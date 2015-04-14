package org.iatoki.judgels.uriel;

public final class ContestContestant {

    private final long id;

    private final String contestJid;

    private final String userJid;

    private final ContestContestantStatus status;

    private final long contestEnterTime;

    public ContestContestant(long id, String contestJid, String userJid, ContestContestantStatus status, long contestEnterTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.status = status;
        this.contestEnterTime = contestEnterTime;
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

    public long getContestEnterTime() {
        return contestEnterTime;
    }
}
