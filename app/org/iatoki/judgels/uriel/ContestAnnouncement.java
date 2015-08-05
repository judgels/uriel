package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestAnnouncement {

    private final long id;
    private final String jid;
    private final String contestJid;
    private final String title;
    private final String content;
    private final String authorJid;
    private final ContestAnnouncementStatus status;
    private final Date lastUpdated;

    public ContestAnnouncement(long id, String jid, String contestJid, String title, String content, String authorJid, ContestAnnouncementStatus status, Date lastUpdated) {
        this.id = id;
        this.jid = jid;
        this.contestJid = contestJid;
        this.title = title;
        this.content = content;
        this.authorJid = authorJid;
        this.status = status;
        this.lastUpdated = lastUpdated;
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

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorJid() {
        return authorJid;
    }

    public ContestAnnouncementStatus getStatus() {
        return status;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
}
