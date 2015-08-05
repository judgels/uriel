package org.iatoki.judgels.uriel;

public final class ContestManager {

    private final long id;

    private final String contestJid;

    private final String userJid;

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
