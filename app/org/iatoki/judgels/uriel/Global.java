package org.iatoki.judgels.uriel;

import akka.actor.Scheduler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.gabriel.commons.GradingResponsePoller;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.sealtiel.client.Sealtiel;
import org.iatoki.judgels.uriel.controllers.ApplicationController;
import org.iatoki.judgels.uriel.controllers.ContestAnnouncementController;
import org.iatoki.judgels.uriel.controllers.ContestClarificationController;
import org.iatoki.judgels.uriel.controllers.ContestContestantController;
import org.iatoki.judgels.uriel.controllers.ContestController;
import org.iatoki.judgels.uriel.controllers.ContestManagerController;
import org.iatoki.judgels.uriel.controllers.ContestProblemController;
import org.iatoki.judgels.uriel.controllers.ContestScoreboardController;
import org.iatoki.judgels.uriel.controllers.ContestSubmissionController;
import org.iatoki.judgels.uriel.controllers.ContestSupervisorController;
import org.iatoki.judgels.uriel.controllers.ContestTeamController;
import org.iatoki.judgels.uriel.controllers.UserRoleController;
import org.iatoki.judgels.uriel.controllers.apis.ContestAPIController;
import org.iatoki.judgels.uriel.models.daos.hibernate.AvatarCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestAnnouncementHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestClarificationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestConfigurationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestContestantHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestManagerHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestProblemHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestReadHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestScoreboardHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestSupervisorHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestTeamCoachHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestTeamHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestTeamMemberHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.GradingHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.JidCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.SubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.UserRoleHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestConfigurationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestReadDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import play.Application;
import play.Play;
import play.libs.Akka;
import play.mvc.Controller;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Global extends org.iatoki.judgels.commons.Global {

    private Map<Class, Controller> cache;

    private SubmissionService submissionService;
    private ContestService contestService;

    public Global() {
    }

    @Override
    public void onStart(Application application) {
        cache = new HashMap<>();

        super.onStart(application);

        Config config = ConfigFactory.load();

        UrielProperties.getInstance();

        JidCacheService.getInstance().setDao(new JidCacheHibernateDao());
        AvatarCacheService.getInstance().setDao(new AvatarCacheHibernateDao());

        Sealtiel sealtiel = new Sealtiel(config.getString("sealtiel.clientJid"), config.getString("sealtiel.clientSecret"), Play.application().configuration().getString("sealtiel.baseUrl"));

        submissionService = new SubmissionServiceImpl(new SubmissionHibernateDao(), new GradingHibernateDao(), sealtiel, Play.application().configuration().getString("sealtiel.gabrielClientJid"));
        GradingResponsePoller poller = new GradingResponsePoller(submissionService, sealtiel);

        ContestDao contestDao = new ContestHibernateDao();
        ContestAnnouncementDao contestAnnouncementDao = new ContestAnnouncementHibernateDao();
        ContestContestantDao contestContestantDao = new ContestContestantHibernateDao();
        ContestTeamDao contestTeamDao = new ContestTeamHibernateDao();
        ContestTeamCoachDao contestTeamCoachDao = new ContestTeamCoachHibernateDao();
        ContestTeamMemberDao contestTeamMemberDao = new ContestTeamMemberHibernateDao();
        ContestClarificationDao contestClarificationDao = new ContestClarificationHibernateDao();
        ContestProblemDao contestProblemDao = new ContestProblemHibernateDao();
        ContestSupervisorDao contestSupervisorDao = new ContestSupervisorHibernateDao();
        ContestManagerDao contestManagerDao = new ContestManagerHibernateDao();
        ContestScoreboardDao contestScoreboardDao = new ContestScoreboardHibernateDao();
        ContestConfigurationDao contestConfigurationDao = new ContestConfigurationHibernateDao();
        ContestReadDao contestReadDao = new ContestReadHibernateDao();
        contestService = new ContestServiceImpl(contestDao, contestAnnouncementDao, contestProblemDao, contestClarificationDao, contestContestantDao, contestTeamDao, contestTeamCoachDao, contestTeamMemberDao, contestSupervisorDao, contestManagerDao, contestScoreboardDao, contestConfigurationDao, contestReadDao);

        ScoreUpdater updater = new ScoreUpdater(contestService, submissionService);

        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), updater, context);
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
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ContestController contestController = new ContestController(contestService);
                cache.put(ContestController.class, contestController);
            } else if (controllerClass.equals(ContestAnnouncementController.class)) {
                ContestAnnouncementController contestAnnouncementController = new ContestAnnouncementController(contestService);
                cache.put(ContestAnnouncementController.class, contestAnnouncementController);
            } else if (controllerClass.equals(ContestClarificationController.class)) {
                ContestClarificationController contestClarificationController = new ContestClarificationController(contestService);
                cache.put(ContestClarificationController.class, contestClarificationController);
            } else if (controllerClass.equals(ContestContestantController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ContestContestantController contestContestantController = new ContestContestantController(contestService, userRoleService);
                cache.put(ContestContestantController.class, contestContestantController);
            } else if (controllerClass.equals(ContestManagerController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ContestManagerController contestManagerController = new ContestManagerController(contestService, userRoleService);
                cache.put(ContestManagerController.class, contestManagerController);
            } else if (controllerClass.equals(ContestProblemController.class)) {
                ContestProblemController contestProblemController = new ContestProblemController(contestService, submissionService);
                cache.put(ContestProblemController.class, contestProblemController);
            } else if (controllerClass.equals(ContestScoreboardController.class)) {
                ContestScoreboardController contestScoreboardController = new ContestScoreboardController(contestService, submissionService);
                cache.put(ContestScoreboardController.class, contestScoreboardController);
            } else if (controllerClass.equals(ContestSubmissionController.class)) {
                ContestSubmissionController contestSubmissionController = new ContestSubmissionController(contestService, submissionService);
                cache.put(ContestSubmissionController.class, contestSubmissionController);
            } else if (controllerClass.equals(ContestSupervisorController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ContestSupervisorController contestSupervisorController = new ContestSupervisorController(contestService, userRoleService);
                cache.put(ContestSupervisorController.class, contestSupervisorController);
            } else if (controllerClass.equals(ContestTeamController.class)) {
                ContestTeamController contestTeamController = new ContestTeamController(contestService);
                cache.put(ContestTeamController.class, contestTeamController);
            } else if (controllerClass.equals(ContestAPIController.class)) {
                ContestAPIController contestAPIController = new ContestAPIController(contestService);
                cache.put(ContestAPIController.class, contestAPIController);
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
