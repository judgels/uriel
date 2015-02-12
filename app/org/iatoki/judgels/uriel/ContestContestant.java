package org.iatoki.judgels.uriel;

public final class ContestContestant {

    private long id;

    private String contestJid;

    private String userJid;

    private ContestContestantStatus status;

    public ContestContestant(long id, String contestJid, String userJid, ContestContestantStatus status) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.status = status;
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
}
