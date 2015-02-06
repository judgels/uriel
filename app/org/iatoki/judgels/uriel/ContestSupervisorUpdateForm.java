package org.iatoki.judgels.uriel;

public final class ContestSupervisorUpdateForm {

    public ContestSupervisorUpdateForm() {
    }

    public ContestSupervisorUpdateForm(ContestPermission contestPermission) {
        this.announcement = contestPermission.isAnnouncement();
        this.problem = contestPermission.isProblem();
        this.submission = contestPermission.isSubmission();
        this.clarification = contestPermission.isClarification();
        this.contestant = contestPermission.isContestant();
    }

    public boolean announcement;

    public boolean problem;

    public boolean submission;

    public boolean clarification;

    public boolean contestant;
}
