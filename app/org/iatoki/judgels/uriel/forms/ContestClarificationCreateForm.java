package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestClarificationCreateForm {

    @Constraints.Required
    public String title;

    @Constraints.Required
    public String question;

    @Constraints.Required
    public String topicJid;
}
