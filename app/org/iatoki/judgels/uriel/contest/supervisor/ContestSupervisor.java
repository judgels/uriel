package org.iatoki.judgels.uriel.contest.supervisor;

public final class ContestSupervisor {

    private final long id;
    private final String contestJid;
    private final String userJid;
    private final ContestPermission contestPermission;

    public ContestSupervisor(long id, String contestJid, String userJid, ContestPermission contestPermission) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.contestPermission = contestPermission;
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

    public ContestPermission getContestPermission() {
        return contestPermission;
    }
}
