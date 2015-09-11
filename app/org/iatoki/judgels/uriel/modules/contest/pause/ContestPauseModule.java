package org.iatoki.judgels.uriel.modules.contest.pause;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;

public final class ContestPauseModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.PAUSE;
    }
}
