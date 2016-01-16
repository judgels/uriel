package org.iatoki.judgels.uriel.contest.team.coach;

import play.data.validation.Constraints;

public final class ContestTeamCoachAddForm {

    @Constraints.Required
    public String username;
}
