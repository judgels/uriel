package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestAnnouncementUpsertForm {

    public ContestAnnouncementUpsertForm() {
    }

    public ContestAnnouncementUpsertForm(ContestAnnouncement contestAnnouncement) {
        this.title = contestAnnouncement.getTitle();
        this.content = contestAnnouncement.getContent();
        this.status = contestAnnouncement.getStatus().name();
    }

    @Constraints.Required
    public String title;

    public String content;

    @Constraints.Required
    public String status;

}
