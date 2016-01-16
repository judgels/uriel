package org.iatoki.judgels.uriel.contest.module.exclusive;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;

public final class ContestExclusiveModule extends ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.EXCLUSIVE;
    }
}
