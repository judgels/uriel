package org.iatoki.judgels.uriel.modules.contest.limited;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;

public final class ContestLimitedModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.LIMITED;
    }
}
