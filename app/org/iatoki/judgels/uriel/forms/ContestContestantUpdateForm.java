package org.iatoki.judgels.uriel.forms;

import org.iatoki.judgels.uriel.ContestContestant;

public final class ContestContestantUpdateForm {

    public ContestContestantUpdateForm() {
    }

    public ContestContestantUpdateForm(ContestContestant contestContestant) {
        this.status = contestContestant.getStatus().name();
    }

    public String status;

}
