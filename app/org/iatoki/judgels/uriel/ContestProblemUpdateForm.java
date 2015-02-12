package org.iatoki.judgels.uriel;

public final class ContestProblemUpdateForm {

    public ContestProblemUpdateForm(ContestProblem contestProblem) {
        this.problemSecret = contestProblem.getProblemSecret();
        this.alias = contestProblem.getAlias();
        this.submissionLimit = contestProblem.getSubmissionLimit();
        this.status = contestProblem.getStatus().name();
    }

    public String problemSecret;

    public String alias;

    public long submissionLimit;

    public String status;

}
