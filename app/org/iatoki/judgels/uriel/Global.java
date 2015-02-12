package org.iatoki.judgels.uriel;

import akka.actor.Scheduler;
import org.iatoki.judgels.gabriel.FakeSealtiel;
import org.iatoki.judgels.gabriel.commons.GradingResponsePoller;
import org.iatoki.judgels.uriel.controllers.ApplicationController;
import org.iatoki.judgels.uriel.controllers.ContestController;
import org.iatoki.judgels.uriel.controllers.UserRoleController;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestAnnouncementHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestClarificationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestContestantHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestManagerHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestSupervisorHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestProblemHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestSubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.JidCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.UserRoleHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import play.Application;
import play.libs.Akka;
import play.mvc.Controller;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Global extends org.iatoki.judgels.commons.Global {

    private Map<Class, Controller> cache;

    @Override
    public void onStart(Application application) {
        cache = new HashMap<>();

        super.onStart(application);

        UrielProperties.getInstance();
        JidCacheService.getInstance().setDao(new JidCacheHibernateDao());

        GradingResponsePoller poller = new GradingResponsePoller(new SubmissionUpdaterServiceImpl(new ContestSubmissionHibernateDao()), new FakeSealtiel(new File("/Users/fushar/grading-requests"), new File("/Users/fushar/grading-responses")));

        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!cache.containsKey(controllerClass)) {
            if (controllerClass.equals(ApplicationController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ApplicationController applicationController = new ApplicationController(userRoleService);
                cache.put(ApplicationController.class, applicationController);
            } else if (controllerClass.equals(ContestController.class)) {
                ContestDao contestDao = new ContestHibernateDao();
                ContestAnnouncementDao contestAnnouncementDao = new ContestAnnouncementHibernateDao();
                ContestContestantDao contestContestantDao = new ContestContestantHibernateDao();
                ContestClarificationDao contestClarificationDao = new ContestClarificationHibernateDao();
                ContestProblemDao contestProblemDao = new ContestProblemHibernateDao();
                ContestSubmissionDao submissionDao = new ContestSubmissionHibernateDao();
                ContestSupervisorDao contestSupervisorDao = new ContestSupervisorHibernateDao();
                ContestManagerDao contestManagerDao = new ContestManagerHibernateDao();
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                ContestService contestService = new ContestServiceImpl(contestDao, contestAnnouncementDao, contestProblemDao, contestClarificationDao, contestContestantDao, contestSupervisorDao, contestManagerDao, userRoleDao);
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);
                FakeSealtiel sealtiel = new FakeSealtiel(new File("/Users/fushar/grading-requests"), new File("/Users/fushar/grading-responses"));
                ContestSubmissionService submissionService = new ContestSubmissionServiceImpl(submissionDao, sealtiel);

                ContestController contestController = new ContestController(contestService, userRoleService, submissionService);
                cache.put(ContestController.class, contestController);
            } else if (controllerClass.equals(UserRoleController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                UserRoleController userRoleController = new UserRoleController(userRoleService);
                cache.put(UserRoleController.class, userRoleController);
            }
        }
        return controllerClass.cast(cache.get(controllerClass));
    }
}
