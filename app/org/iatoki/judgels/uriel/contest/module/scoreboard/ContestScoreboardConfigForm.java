package org.iatoki.judgels.uriel.contest.module.scoreboard;

import play.data.validation.Constraints;

public final class ContestScoreboardConfigForm {

    @Constraints.Required
    public boolean isIncognitoScoreboard;
}
