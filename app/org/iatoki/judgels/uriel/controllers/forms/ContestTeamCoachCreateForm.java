package org.iatoki.judgels.uriel.controllers.forms;

import play.data.validation.Constraints;

public final class ContestTeamCoachCreateForm {

    @Constraints.Required
    public String username;
}
