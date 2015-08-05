package org.iatoki.judgels.uriel.forms;

import org.iatoki.judgels.uriel.ContestClarification;
import play.data.validation.Constraints;

public final class ContestClarificationChangeForm {

    public ContestClarificationChangeForm() {
    }

    public ContestClarificationChangeForm(ContestClarification contestClarification) {
        this.title = contestClarification.getTitle();
        this.question = contestClarification.getQuestion();
    }

    @Constraints.Required
    public String title;

    @Constraints.Required
    public String question;

}
