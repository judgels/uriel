package org.iatoki.judgels.uriel;

public final class ContestContestantUpdateForm {

    public ContestContestantUpdateForm() {
    }

    public ContestContestantUpdateForm(ContestContestant contestContestant) {
        this.status = contestContestant.getStatus().name();
    }

    public String status;

}
