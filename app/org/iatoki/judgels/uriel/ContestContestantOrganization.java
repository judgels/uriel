package org.iatoki.judgels.uriel;

public class ContestContestantOrganization {

    private final long id;
    private final String userJid;
    private final String contestJid;

    public ContestContestantOrganization(long id, String contestJid, String userJid) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getContestJid() {
        return contestJid;
    }
}
