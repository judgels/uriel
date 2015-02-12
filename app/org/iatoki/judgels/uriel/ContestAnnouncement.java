package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestAnnouncement {

    private final long id;

    private final String contestJid;

    private final String title;

    private final String announcement;

    private final String authorJid;

    private final ContestAnnouncementStatus status;

    private final Date lastUpdated;

    public ContestAnnouncement(long id, String contestJid, String title, String announcement, String authorJid, ContestAnnouncementStatus status, Date lastUpdated) {
        this.id = id;
        this.contestJid = contestJid;
        this.title = title;
        this.announcement = announcement;
        this.authorJid = authorJid;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getTitle() {
        return title;
    }

    public String getAnnouncement() {
        return announcement;
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
