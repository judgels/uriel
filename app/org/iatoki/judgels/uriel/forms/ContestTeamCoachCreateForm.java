package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestTeamCoachCreateForm {

    @Constraints.Required
    public String username;
}
