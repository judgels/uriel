package org.iatoki.judgels.uriel.controllers.forms;

import org.iatoki.judgels.uriel.ContestAnnouncement;
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
