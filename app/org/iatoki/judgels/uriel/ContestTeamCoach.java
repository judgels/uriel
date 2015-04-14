package org.iatoki.judgels.uriel;

public final class ContestTeamCoach {

    private final long id;

    private final String teamJid;

    private final String coachJid;

    public ContestTeamCoach(long id, String teamJid, String coachJid) {
        this.id = id;
        this.teamJid = teamJid;
        this.coachJid = coachJid;
    }

    public long getId() {
        return id;
    }

    public String getTeamJid() {
        return teamJid;
    }

    public String getCoachJid() {
        return coachJid;
    }
}
