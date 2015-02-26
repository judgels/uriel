package org.iatoki.judgels.uriel;

import java.net.URL;

public final class ContestTeam {

    private long id;

    private String jid;

    private String contestJid;

    private String name;

    private URL teamImage;

    public ContestTeam(long id, String jid, String contestJid, String name, URL teamImage) {
        this.id = id;
        this.jid = jid;
        this.contestJid = contestJid;
        this.name = name;
        this.teamImage = teamImage;
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
}
