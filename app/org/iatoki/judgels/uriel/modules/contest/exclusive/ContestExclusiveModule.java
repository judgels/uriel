package org.iatoki.judgels.uriel.modules.contest.exclusive;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;

public final class ContestExclusiveModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.EXCLUSIVE;
    }
}
