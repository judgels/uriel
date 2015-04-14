package org.iatoki.judgels.uriel;

import java.net.URL;
import java.util.List;

public final class ContestTeam {

    private long id;

    private String jid;

    private String contestJid;

    private String name;

    private URL teamImage;

    private List<ContestTeamCoach> coaches;

    private List<ContestTeamMember> members;

    public ContestTeam(long id, String jid, String contestJid, String name, URL teamImage, List<ContestTeamCoach> coaches, List<ContestTeamMember> members) {
        this.id = id;
        this.jid = jid;
        this.contestJid = contestJid;
        this.name = name;
        this.teamImage = teamImage;
        this.coaches = coaches;
        this.members = members;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getName() {
        return name;
    }

    public URL getTeamImage() {
        return teamImage;
    }

    public List<ContestTeamCoach> getCoaches() {
        return coaches;
    }

    public List<ContestTeamMember> getMembers() {
        return members;
    }
}
