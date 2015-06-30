package org.iatoki.judgels.uriel;

import akka.actor.Scheduler;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.DefaultUserActivityMessageServiceImpl;
import org.iatoki.judgels.sandalphon.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.config.ControllerConfig;
import org.iatoki.judgels.uriel.config.PersistenceConfig;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.ControllerUtils;
import org.iatoki.judgels.uriel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.uriel.models.daos.JidCacheDao;
import org.iatoki.judgels.uriel.services.AvatarCacheService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestPasswordService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.services.impls.JidCacheService;
import org.iatoki.judgels.uriel.services.impls.UserActivityMessageServiceImpl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import play.Application;
import play.libs.Akka;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class Global extends org.iatoki.judgels.commons.Global {

    private ApplicationContext applicationContext;

    @Override
    public void onStart(Application application) {
        applicationContext = new AnnotationConfigApplicationContext(PersistenceConfig.class, ControllerConfig.class);
        buildServices();
        buildUtils();
        scheduleThreads();
        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return getContextBean(controllerClass).orElse(super.getControllerInstance(controllerClass));
    }

    private <A> Optional<A> getContextBean(Class<A> controllerClass) throws Exception {
        if (applicationContext == null) {
            throw new Exception("Application Context not Initialized");
        } else {
            try {
                return Optional.of(applicationContext.getBean(controllerClass));
            } catch (NoSuchBeanDefinitionException ex) {
                return Optional.empty();
            }
        }
    }

    private void buildServices() {
        JidCacheService.buildInstance(applicationContext.getBean(JidCacheDao.class));
        AvatarCacheService.buildInstance(applicationContext.getBean(Jophiel.class), applicationContext.getBean(AvatarCacheDao.class));
        DefaultUserActivityMessageServiceImpl.buildInstance(applicationContext.getBean(Jophiel.class));
    }

    private void buildUtils() {
        ControllerUtils.buildInstance(applicationContext.getBean(Jophiel.class));
        ContestControllerUtils.buildInstance(applicationContext.getBean(ContestService.class), applicationContext.getBean(ContestContestantService.class), applicationContext.getBean(ContestSupervisorService.class), applicationContext.getBean(ContestManagerService.class), applicationContext.getBean(ContestTeamService.class), applicationContext.getBean(ContestPasswordService.class));
    }

    private void scheduleThreads() {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, applicationContext.getBean(SubmissionService.class), applicationContext.getBean(Sealtiel.class), TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        ScoreUpdater updater = new ScoreUpdater(applicationContext.getBean(ContestService.class), applicationContext.getBean(ContestScoreboardService.class), applicationContext.getBean(SubmissionService.class));
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(applicationContext.getBean(Jophiel.class), applicationContext.getBean(UserService.class), UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), updater, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
