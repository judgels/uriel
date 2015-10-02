package org.iatoki.judgels.uriel.modules.contest;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.modules.contest.clarification.ContestClarificationModule;
import org.iatoki.judgels.uriel.modules.contest.clarificationtimelimit.ContestClarificationTimeLimitModule;
import org.iatoki.judgels.uriel.modules.contest.file.ContestFileModule;
import org.iatoki.judgels.uriel.modules.contest.frozenscoreboard.ContestFrozenScoreboardModule;
import org.iatoki.judgels.uriel.modules.contest.limited.ContestLimitedModule;
import org.iatoki.judgels.uriel.modules.contest.password.ContestPasswordModule;
import org.iatoki.judgels.uriel.modules.contest.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.exclusive.ContestExclusiveModule;
import org.iatoki.judgels.uriel.modules.contest.pause.ContestPauseModule;
import org.iatoki.judgels.uriel.modules.contest.scoreboard.ContestScoreboardModule;
import org.iatoki.judgels.uriel.modules.contest.supervisor.ContestSupervisorModule;
import org.iatoki.judgels.uriel.modules.contest.team.ContestTeamModule;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTrigger;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTriggerModule;
import org.iatoki.judgels.uriel.modules.contest.virtual.ContestVirtualModule;

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
            case LIMITED:
                return new ContestLimitedModule();
            case DURATION:
                return new ContestDurationModule(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)), TimeUnit.MILLISECONDS.convert(5, TimeUnit.HOURS));
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
            case LIMITED:
                return new Gson().fromJson(config, ContestLimitedModule.class);
            case DURATION:
                return new Gson().fromJson(config, ContestDurationModule.class);
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
            default:
                throw new RuntimeException();
        }
    }
}
