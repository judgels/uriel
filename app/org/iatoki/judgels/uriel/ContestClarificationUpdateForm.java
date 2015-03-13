package org.iatoki.judgels.uriel;

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
