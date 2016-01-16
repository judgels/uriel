package org.iatoki.judgels.uriel.contest.module.pause;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;

public final class ContestPauseModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.PAUSE;
    }
}
