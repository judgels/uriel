package org.iatoki.judgels.uriel.contest.problem;

import play.data.validation.Constraints;

public final class ContestProblemEditForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public long submissionsLimit;

    @Constraints.Required
    public String status;
}
