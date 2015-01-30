package org.iatoki.judgels.uriel;

public final class ContestProblemUpdateForm {

    public ContestProblemUpdateForm(ContestProblem contestProblem) {
        this.problemSecret = contestProblem.getProblemSecret();
        this.alias = contestProblem.getAlias();
        this.name = contestProblem.getName();
        this.submissionLimit = contestProblem.getSubmissionLimit();
        this.status = contestProblem.getStatus().name();
    }

    public String problemSecret;

    public String alias;

    public String name;

    public long submissionLimit;

    public String status;

}
