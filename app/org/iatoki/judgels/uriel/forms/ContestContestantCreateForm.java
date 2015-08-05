package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestContestantCreateForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String status;
}
