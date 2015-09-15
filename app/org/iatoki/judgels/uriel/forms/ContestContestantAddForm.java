package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestContestantAddForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String status;
}
