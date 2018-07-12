package org.iatoki.judgels.uriel.contest.module;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.contest.module.clarification.ContestClarificationModule;
import org.iatoki.judgels.uriel.contest.module.clarificationtimelimit.ContestClarificationTimeLimitModule;
import org.iatoki.judgels.uriel.contest.module.delayedgrading.DelayedGradingModule;
import org.iatoki.judgels.uriel.contest.module.file.ContestFileModule;
import org.iatoki.judgels.uriel.contest.module.frozenscoreboard.ContestFrozenScoreboardModule;
import org.iatoki.judgels.uriel.contest.module.javaspecification.ContestJavaSpecificationModule;
import org.iatoki.judgels.uriel.contest.module.limited.ContestLimitedModule;
import org.iatoki.judgels.uriel.contest.module.exclusive.ContestExclusiveModule;
import org.iatoki.judgels.uriel.contest.module.supervisor.ContestSupervisorModule;
import org.iatoki.judgels.uriel.contest.module.trigger.ContestTriggerModule;
import org.iatoki.judgels.uriel.contest.module.virtual.ContestVirtualModule;
import org.iatoki.judgels.uriel.contest.module.organization.ContestOrganizationModule;
import org.iatoki.judgels.uriel.contest.module.password.ContestPasswordModule;
import org.iatoki.judgels.uriel.contest.module.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.contest.module.pause.ContestPauseModule;
import org.iatoki.judgels.uriel.contest.module.scoreboard.ContestScoreboardModule;
import org.iatoki.judgels.uriel.contest.module.team.ContestTeamModule;
import org.iatoki.judgels.uriel.contest.module.trigger.ContestTrigger;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class ContestModuleFactory {

    private ContestModuleFactory() {
        // prevent instantiation
    }

    public static ContestModule createDefaultContestModule(ContestModules contestModules) {
        switch (contestModules) {
            case CLARIFICATION:
                return new ContestClarificationModule();
            case CLARIFICATION_TIME_LIMIT:
                return new ContestClarificationTimeLimitModule(0);
            case DELAYED_GRADING:
                return new DelayedGradingModule(0);
            case LIMITED:
                return new ContestLimitedModule();
            case EXCLUSIVE:
                return new ContestExclusiveModule();
            case REGISTRATION:
                return new ContestRegistrationModule(new Date().getTime(), false, TimeUnit.MILLISECONDS.convert(5, TimeUnit.DAYS), 0);
            case PAUSE:
                return new ContestPauseModule();
            case SCOREBOARD:
                return new ContestScoreboardModule(false);
            case FROZEN_SCOREBOARD:
                return new ContestFrozenScoreboardModule(false, 0);
            case SUPERVISOR:
                return new ContestSupervisorModule();
            case TEAM:
                return new ContestTeamModule();
            case TRIGGER:
                return new ContestTriggerModule(ContestTrigger.COACH);
            case VIRTUAL:
                return new ContestVirtualModule(TimeUnit.MILLISECONDS.convert(5, TimeUnit.HOURS));
            case FILE:
                return new ContestFileModule();
            case PASSWORD:
                return new ContestPasswordModule();
            case ORGANIZATION:
                return new ContestOrganizationModule();
            case JAVA_SPECIFICATION:
                return new ContestJavaSpecificationModule();
            default:
                throw new RuntimeException();
        }
    }

    public static ContestModule parseFromConfig(ContestModules contestModules, String config) {
        switch (contestModules) {
            case CLARIFICATION:
                return new Gson().fromJson(config, ContestClarificationModule.class);
            case CLARIFICATION_TIME_LIMIT:
                return new Gson().fromJson(config, ContestClarificationTimeLimitModule.class);
            case DELAYED_GRADING:
                return new Gson().fromJson(config, DelayedGradingModule.class);
            case LIMITED:
                return new Gson().fromJson(config, ContestLimitedModule.class);
            case EXCLUSIVE:
                return new Gson().fromJson(config, ContestExclusiveModule.class);
            case REGISTRATION:
                return new Gson().fromJson(config, ContestRegistrationModule.class);
            case PAUSE:
                return new Gson().fromJson(config, ContestPauseModule.class);
            case SCOREBOARD:
                return new Gson().fromJson(config, ContestScoreboardModule.class);
            case FROZEN_SCOREBOARD:
                return new Gson().fromJson(config, ContestFrozenScoreboardModule.class);
            case SUPERVISOR:
                return new Gson().fromJson(config, ContestSupervisorModule.class);
            case TRIGGER:
                return new Gson().fromJson(config, ContestTriggerModule.class);
            case TEAM:
                return new Gson().fromJson(config, ContestTeamModule.class);
            case VIRTUAL:
                return new Gson().fromJson(config, ContestVirtualModule.class);
            case FILE:
                return new Gson().fromJson(config, ContestFileModule.class);
            case PASSWORD:
                return new Gson().fromJson(config, ContestPasswordModule.class);
            case ORGANIZATION:
                return new Gson().fromJson(config, ContestOrganizationModule.class);
            case JAVA_SPECIFICATION:
                return new Gson().fromJson(config, ContestJavaSpecificationModule.class);
            default:
                throw new RuntimeException();
        }
    }
}
