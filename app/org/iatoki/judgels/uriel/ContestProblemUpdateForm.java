package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestProblemUpdateForm {

    public ContestProblemUpdateForm() {

    }

    public ContestProblemUpdateForm(ContestProblem contestProblem) {
        this.problemSecret = contestProblem.getProblemSecret();
        this.alias = contestProblem.getAlias();
        this.submissionsLimit = contestProblem.getSubmissionsLimit();
        this.status = contestProblem.getStatus().name();
    }

    @Constraints.Required
    public String problemSecret;


    @Constraints.Required
    public String alias;


    @Constraints.Required
    public long submissionsLimit;


    @Constraints.Required
    public String status;
}
