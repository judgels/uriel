package org.iatoki.judgels.uriel.forms;

import org.iatoki.judgels.uriel.ContestSupervisor;

public final class ContestSupervisorUpdateForm {

    public ContestSupervisorUpdateForm() {
    }

    public ContestSupervisorUpdateForm(ContestSupervisor contestSupervisor) {
        this.announcement = contestSupervisor.isAnnouncement();
        this.problem = contestSupervisor.isProblem();
        this.submission = contestSupervisor.isSubmission();
        this.clarification = contestSupervisor.isClarification();
        this.contestant = contestSupervisor.isContestant();
    }

    public boolean announcement;

    public boolean problem;

    public boolean submission;

    public boolean clarification;

    public boolean contestant;
}
