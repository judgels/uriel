package org.iatoki.judgels.uriel.modules;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.modules.clarification.ContestClarificationModule;
import org.iatoki.judgels.uriel.modules.file.ContestFileModule;
import org.iatoki.judgels.uriel.modules.limited.ContestLimitedModule;
import org.iatoki.judgels.uriel.modules.password.ContestPasswordModule;
import org.iatoki.judgels.uriel.modules.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.exclusive.ContestExclusiveModule;
import org.iatoki.judgels.uriel.modules.pause.ContestPauseModule;
import org.iatoki.judgels.uriel.modules.scoreboard.ContestScoreboardModule;
import org.iatoki.judgels.uriel.modules.supervisor.ContestSupervisorModule;
import org.iatoki.judgels.uriel.modules.team.ContestTeamModule;
import org.iatoki.judgels.uriel.modules.trigger.ContestTrigger;
import org.iatoki.judgels.uriel.modules.trigger.ContestTriggerModule;
import org.iatoki.judgels.uriel.modules.virtual.ContestVirtualModule;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class ContestModuleFactory {

    public ContestModule createDefaultContestModule(ContestModules contestModules) {
        switch (contestModules) {
            case CLARIFICATION:
                return new ContestClarificationModule(0);
            case LIMITED:
                return new ContestLimitedModule();
            case DURATION:
                return new ContestDurationModule(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)), TimeUnit.SECONDS.convert(5, TimeUnit.HOURS));
            case EXCLUSIVE:
                return new ContestExclusiveModule();
            case REGISTRATION:
                return new ContestRegistrationModule(new Date(), TimeUnit.SECONDS.convert(5, TimeUnit.DAYS), 0);
            case PAUSE:
                return new ContestPauseModule();
            case SCOREBOARD:
                return new ContestScoreboardModule(true, false, 0);
            case SUPERVISOR:
                return new ContestSupervisorModule();
            case TEAM:
                return new ContestTeamModule();
            case TRIGGER:
                return new ContestTriggerModule(ContestTrigger.TEAM_MEMBER);
            case VIRTUAL:
                return new ContestVirtualModule(TimeUnit.SECONDS.convert(5, TimeUnit.HOURS));
            case FILE:
                return new ContestFileModule();
            case PASSWORD:
                return new ContestPasswordModule();
            default:
                throw new RuntimeException();
        }
    }

    public ContestModule parseFromConfig(ContestModules contestModules, String config) {
        switch (contestModules) {
            case CLARIFICATION:
                return new Gson().fromJson(config, ContestClarificationModule.class);
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
