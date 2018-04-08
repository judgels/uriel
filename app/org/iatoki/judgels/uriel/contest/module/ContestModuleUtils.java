package org.iatoki.judgels.uriel.contest.module;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ContestModuleUtils {

    private ContestModuleUtils() {
        // prevent instantiation
    }

    public static List<ContestModules> getDependedModules(ContestModules contestModules) {
        switch (contestModules) {
            case VIRTUAL:
                return ImmutableList.of(ContestModules.TRIGGER);
            case TEAM:
                return ImmutableList.of(ContestModules.TRIGGER);
            default:
                return ImmutableList.of();
        }
    }

    public static List<ContestModules> getModuleDependencies(ContestModules contestModules) {
        switch (contestModules) {
            case CLARIFICATION_TIME_LIMIT:
                return ImmutableList.of(ContestModules.CLARIFICATION);
            case FROZEN_SCOREBOARD:
                return ImmutableList.of(ContestModules.SCOREBOARD);
            case TRIGGER:
                return ImmutableList.of(ContestModules.VIRTUAL, ContestModules.TEAM);
            case ORGANIZATION:
                return ImmutableList.of(ContestModules.REGISTRATION);
            default:
                return ImmutableList.of();
        }
    }

    public static List<ContestModules> getModuleContradiction(ContestModules contestModules) {
        switch (contestModules) {
            case FROZEN_SCOREBOARD:
                return ImmutableList.of(ContestModules.VIRTUAL);
            case VIRTUAL:
                return ImmutableList.of(ContestModules.FROZEN_SCOREBOARD);
            case LIMITED:
                return ImmutableList.of(ContestModules.REGISTRATION);
            case REGISTRATION:
                return ImmutableList.of(ContestModules.LIMITED);
            default:
                return ImmutableList.of();
        }
    }
}
