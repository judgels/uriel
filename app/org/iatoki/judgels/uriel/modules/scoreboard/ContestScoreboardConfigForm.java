package org.iatoki.judgels.uriel.modules.scoreboard;

import play.data.validation.Constraints;

public final class ContestScoreboardConfigForm {

    @Constraints.Required
    public boolean isIncognitoScoreboard;

    @Constraints.Required
    public boolean isOfficialScoreboardAllowed;

    @Constraints.Required
    public long scoreboardFreezeTime;
}
