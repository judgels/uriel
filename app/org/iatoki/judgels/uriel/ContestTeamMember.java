package org.iatoki.judgels.uriel;

public final class ContestTeamMember {

    private long id;

    private String teamJid;

    private String memberJid;

    public ContestTeamMember(long id, String teamJid, String memberJid) {
        this.id = id;
        this.teamJid = teamJid;
        this.memberJid = memberJid;
    }

    public long getId() {
        return id;
    }

    public String getTeamJid() {
        return teamJid;
    }

    public String getMemberJid() {
        return memberJid;
    }
}
