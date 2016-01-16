package org.iatoki.judgels.uriel.contest.contestant;

import play.data.validation.Constraints;

public final class ContestContestantAddForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String status;
}
