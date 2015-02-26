package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestTeamMemberCreateForm {

    @Constraints.Required
    public String username;
}
