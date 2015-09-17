package org.iatoki.judgels.uriel;

import akka.actor.Scheduler;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;
import org.iatoki.judgels.sandalphon.runnables.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.UrielControllerUtils;
import org.iatoki.judgels.uriel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.uriel.models.daos.JidCacheDao;
import org.iatoki.judgels.uriel.runnables.ScoreboardUpdater;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.services.impls.UrielDataMigrationServiceImpl;
import play.Application;
import play.inject.Injector;
import play.libs.Akka;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public final class Global extends AbstractGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        buildServices(application.injector());
        buildUtils(application.injector());
        scheduleThreads(application.injector());
    }

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new UrielDataMigrationServiceImpl();
    }

    private void buildServices(Injector injector) {
        JidCacheServiceImpl.buildInstance(injector.instanceOf(JidCacheDao.class));
        AvatarCacheServiceImpl.buildInstance(injector.instanceOf(AvatarCacheDao.class));
        UserActivityMessageServiceImpl.buildInstance();
    }

    private void buildUtils(Injector injector) {
        JophielClientControllerUtils.buildInstance(UrielProperties.getInstance().getJophielBaseUrl());
        UrielControllerUtils.buildInstance(injector.instanceOf(JophielClientAPI.class), injector.instanceOf(JophielPublicAPI.class));
        ContestControllerUtils.buildInstance(injector.instanceOf(ContestContestantService.class), injector.instanceOf(ContestSupervisorService.class), injector.instanceOf(ContestManagerService.class), injector.instanceOf(ContestTeamService.class), injector.instanceOf(ContestContestantPasswordService.class));
    }

    private void scheduleThreads(Injector injector) {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, injector.instanceOf(ProgrammingSubmissionService.class), injector.instanceOf(SealtielClientAPI.class), TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        ScoreboardUpdater updater = new ScoreboardUpdater(injector.instanceOf(ContestService.class), injector.instanceOf(ContestScoreboardService.class), injector.instanceOf(ProgrammingSubmissionService.class));
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(injector.instanceOf(JophielClientAPI.class), UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), updater, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
