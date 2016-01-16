package org.iatoki.judgels.uriel.contest.team.member;

import play.data.validation.Constraints;

public final class ContestTeamMemberAddForm {

    @Constraints.Required
    public String username;
}
