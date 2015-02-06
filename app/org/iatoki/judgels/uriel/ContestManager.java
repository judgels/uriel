package org.iatoki.judgels.uriel;

public final class ContestManager {

    private long id;

    private String contestJid;

    private String userJid;

    private String username;

    private String alias;

    public ContestManager(long id, String contestJid, String userJid, String username, String alias) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.username = username;
        this.alias = alias;
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
}
