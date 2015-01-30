package org.iatoki.judgels.uriel;

public final class ContestAnnouncementUpsertForm {

    public ContestAnnouncementUpsertForm() {
    }

    public ContestAnnouncementUpsertForm(ContestAnnouncement contestAnnouncement) {
        this.title = contestAnnouncement.getTitle();
        this.announcement = contestAnnouncement.getAnnouncement();
        this.status = contestAnnouncement.getStatus().name();
    }

    public String title;

    public String announcement;

    public String status;

}
