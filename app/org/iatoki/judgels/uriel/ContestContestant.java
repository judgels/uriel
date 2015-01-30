package org.iatoki.judgels.uriel;

public final class ContestContestant {

    private long id;

    private String contestJid;

    private String username;

    private String alias;

    private ContestContestantStatus status;

    public ContestContestant(long id, String contestJid, String username, String alias, ContestContestantStatus status) {
        this.id = id;
        this.contestJid = contestJid;
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
