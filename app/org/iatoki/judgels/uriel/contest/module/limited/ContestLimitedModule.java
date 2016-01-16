package org.iatoki.judgels.uriel.contest.module.limited;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;

public final class ContestLimitedModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.LIMITED;
    }
}
