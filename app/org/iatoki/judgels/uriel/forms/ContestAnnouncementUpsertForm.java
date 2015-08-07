package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestAnnouncementUpsertForm {

    @Constraints.Required
    public String title;

    public String content;

    @Constraints.Required
    public String status;
}
