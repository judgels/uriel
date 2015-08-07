package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestProblemUpdateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public long submissionsLimit;

    @Constraints.Required
    public String status;
}
