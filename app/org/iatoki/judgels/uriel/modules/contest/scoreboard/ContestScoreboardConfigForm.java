package org.iatoki.judgels.uriel.modules.contest.scoreboard;

import play.data.validation.Constraints;

public final class ContestScoreboardConfigForm {

    @Constraints.Required
    public boolean isIncognitoScoreboard;
}
