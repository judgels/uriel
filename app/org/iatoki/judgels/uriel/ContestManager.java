package org.iatoki.judgels.uriel;

public final class ContestManager {

    private long id;

    private String contestJid;

    private String userJid;

    public ContestManager(long id, String contestJid, String userJid) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
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
}
