package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestTeamCoachAddForm {

    @Constraints.Required
    public String username;
}
