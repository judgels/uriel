package org.iatoki.judgels.uriel.forms;

import org.iatoki.judgels.uriel.ContestProblem;
import play.data.validation.Constraints;

public final class ContestProblemUpdateForm {

    public ContestProblemUpdateForm() {

    }

    public ContestProblemUpdateForm(ContestProblem contestProblem) {
        this.alias = contestProblem.getAlias();
        this.submissionsLimit = contestProblem.getSubmissionsLimit();
        this.status = contestProblem.getStatus().name();
    }

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public long submissionsLimit;


    @Constraints.Required
    public String status;
}
