package org.iatoki.judgels.uriel.modules.contest.frozenscoreboard;

import play.data.validation.Constraints;

public final class ContestFrozenScoreboardConfigForm {

    @Constraints.Required
    public boolean isOfficialScoreboardAllowed;

    @Constraints.Required
    public long scoreboardFreezeTime;
}
