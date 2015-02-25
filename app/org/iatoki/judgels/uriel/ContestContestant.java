package org.iatoki.judgels.uriel;

public final class ContestContestant {

    private long id;

    private String contestJid;

    private String userJid;

    private ContestContestantStatus status;

    private long contestEnterTime;

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
