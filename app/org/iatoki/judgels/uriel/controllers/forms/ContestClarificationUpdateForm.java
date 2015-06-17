package org.iatoki.judgels.uriel.controllers.forms;

import org.iatoki.judgels.uriel.ContestClarification;

public final class ContestClarificationUpdateForm {

    public ContestClarificationUpdateForm() {
    }

    public ContestClarificationUpdateForm(ContestClarification contestClarification) {
        this.answer = contestClarification.getAnswer();
        this.status = contestClarification.getStatus().name();
    }

    public String answer;

    public String status;
}
