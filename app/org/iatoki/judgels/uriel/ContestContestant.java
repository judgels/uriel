package org.iatoki.judgels.uriel;

public final class ContestContestant {

    private long id;

    private String contestJid;

    private String userJid;

    private String username;

    private String alias;

    private ContestContestantStatus status;

    public ContestContestant(long id, String contestJid, String userJid, String username, String alias, ContestContestantStatus status) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.username = username;
        this.alias = alias;
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

    public String getUsername() {
        return username;
    }

    public String getAlias() {
        return alias;
    }

    public ContestContestantStatus getStatus() {
        return status;
    }
}
