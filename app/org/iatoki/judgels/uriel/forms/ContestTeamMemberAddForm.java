package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestTeamMemberAddForm {

    @Constraints.Required
    public String username;
}
