package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestAnnouncement {

    private long id;

    private String contestJid;

    private String title;

    private String announcement;

    private String author;

    private ContestAnnouncementStatus status;

    private Date lastUpdated;

    public ContestAnnouncement(long id, String contestJid, String title, String announcement, String author, ContestAnnouncementStatus status, Date lastUpdated) {
        this.id = id;
        this.contestJid = contestJid;
        this.title = title;
        this.announcement = announcement;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public ContestAnnouncementStatus getStatus() {
        return status;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
}
