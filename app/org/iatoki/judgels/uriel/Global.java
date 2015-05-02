package org.iatoki.judgels.uriel;

import akka.actor.Scheduler;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.gabriel.commons.GradingResponsePoller;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.jophiel.commons.UserActivityPusher;
import org.iatoki.judgels.jophiel.commons.controllers.JophielClientController;
import org.iatoki.judgels.sealtiel.client.Sealtiel;
import org.iatoki.judgels.uriel.controllers.ApplicationController;
import org.iatoki.judgels.uriel.controllers.ContestAnnouncementController;
import org.iatoki.judgels.uriel.controllers.ContestClarificationController;
import org.iatoki.judgels.uriel.controllers.ContestContestantController;
import org.iatoki.judgels.uriel.controllers.ContestController;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.ContestManagerController;
import org.iatoki.judgels.uriel.controllers.ContestProblemController;
import org.iatoki.judgels.uriel.controllers.ContestScoreboardController;
import org.iatoki.judgels.uriel.controllers.ContestSubmissionController;
import org.iatoki.judgels.uriel.controllers.ContestSupervisorController;
import org.iatoki.judgels.uriel.controllers.ContestTeamController;
import org.iatoki.judgels.uriel.controllers.UserController;
import org.iatoki.judgels.uriel.controllers.apis.ContestAPIController;
import org.iatoki.judgels.uriel.controllers.apis.ContestTestingAPIController;
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
import org.iatoki.judgels.uriel.models.daos.hibernate.UserHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.AvatarCacheDao;
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
import org.iatoki.judgels.uriel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.JidCacheDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserDao;
import play.Application;
import play.libs.Akka;
import play.mvc.Controller;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Global extends org.iatoki.judgels.commons.Global {
    private AvatarCacheDao avatarCacheDao;
    private ContestAnnouncementDao contestAnnouncementDao;
    private ContestClarificationDao contestClarificationDao;
    private ContestConfigurationDao contestConfigurationDao;
    private ContestContestantDao contestContestantDao;
    private ContestDao contestDao;
    private ContestManagerDao contestManagerDao;
    private ContestProblemDao contestProblemDao;
    private ContestReadDao contestReadDao;
    private ContestScoreboardDao contestScoreboardDao;
    private ContestSupervisorDao contestSupervisorDao;
    private ContestTeamCoachDao contestTeamCoachDao;
    private ContestTeamDao contestTeamDao;
    private ContestTeamMemberDao contestTeamMemberDao;
    private GradingDao gradingDao;
    private JidCacheDao jidCacheDao;
    private SubmissionDao submissionDao;
    private UserDao userDao;

    private UrielProperties urielProps;

    private Sealtiel sealtiel;

    private FileSystemProvider teamAvatarFileProvider;
    private FileSystemProvider submissionLocalFileProvider;
    private FileSystemProvider submissionRemoteFileProvider;

    private ContestService contestService;
    private SubmissionService submissionService;
    private UserService userService;

    private Map<Class<?>, Controller> controllersRegistry;

    @Override
    public void onStart(Application application) {
        buildDaos();
        buildProperties();
        buildSealtiel();
        buildFileProviders();
        buildServices();
        buildUtils();
        buildControllers();
        scheduleThreads();
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return controllerClass.cast(controllersRegistry.get(controllerClass));
    }

    private void buildDaos() {
        avatarCacheDao = new AvatarCacheHibernateDao();
        contestAnnouncementDao = new ContestAnnouncementHibernateDao();
        contestClarificationDao = new ContestClarificationHibernateDao();
        contestConfigurationDao = new ContestConfigurationHibernateDao();
        contestContestantDao = new ContestContestantHibernateDao();
        contestDao = new ContestHibernateDao();
        contestManagerDao = new ContestManagerHibernateDao();
        contestProblemDao = new ContestProblemHibernateDao();
        contestReadDao = new ContestReadHibernateDao();
        contestScoreboardDao = new ContestScoreboardHibernateDao();
        contestSupervisorDao = new ContestSupervisorHibernateDao();
        contestTeamCoachDao = new ContestTeamCoachHibernateDao();
        contestTeamDao = new ContestTeamHibernateDao();
        contestTeamMemberDao = new ContestTeamMemberHibernateDao();
        gradingDao = new GradingHibernateDao();
        jidCacheDao = new JidCacheHibernateDao();
        submissionDao = new SubmissionHibernateDao();
        userDao = new UserHibernateDao();
    }

    private void buildProperties() {
        Config config = ConfigFactory.load();

        org.iatoki.judgels.uriel.BuildInfo$ buildInfo = org.iatoki.judgels.uriel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), config);

        UrielProperties.buildInstance(config);
        urielProps = UrielProperties.getInstance();
    }

    private void buildSealtiel() {
        sealtiel = new Sealtiel(urielProps.getSealtielClientJid(), urielProps.getSealtielClientSecret(), urielProps.getSealtielBaseUrl());
    }

    private void buildFileProviders() {
        if (urielProps.isTeamAvatarUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProps.isTeamAvatarAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProps.getTeamAvatarAWSAccessKey(), urielProps.getTeamAvatarAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            teamAvatarFileProvider = new AWSFileSystemProvider(awsS3Client, urielProps.getTeamAvatarAWSS3BucketName(), urielProps.getTeamAvatarAWSCloudFrontUrl(), urielProps.getTeamAvatarAWSS3BucketRegion());
        } else {
            teamAvatarFileProvider = new LocalFileSystemProvider(urielProps.getTeamAvatarLocalDir());
        }

        if (urielProps.isSubmissionUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProps.isSubmissionAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProps.getSubmissionAWSAccessKey(), urielProps.getSubmissionAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            submissionRemoteFileProvider = new AWSFileSystemProvider(awsS3Client, urielProps.getSubmissionAWSS3BucketName(), urielProps.getSubmissionAWSS3BucketRegion());
        }
        submissionLocalFileProvider = new LocalFileSystemProvider(urielProps.getSubmissionLocalDir());
    }

    private void buildServices() {
        contestService = new ContestServiceImpl(
                contestDao,
                contestAnnouncementDao,
                contestProblemDao,
                contestClarificationDao,
                contestContestantDao,
                contestTeamDao,
                contestTeamCoachDao,
                contestTeamMemberDao,
                contestSupervisorDao,
                contestManagerDao,
                contestScoreboardDao,
                contestConfigurationDao,
                contestReadDao,
                teamAvatarFileProvider
        );

        submissionService = new SubmissionServiceImpl(submissionDao, gradingDao, sealtiel, urielProps.getSealtielGabrielClientJid());
        userService = new UserServiceImpl(userDao);

        JidCacheService.getInstance().setDao(jidCacheDao);
        AvatarCacheService.getInstance().setDao(avatarCacheDao);
    }

    private void buildUtils() {
        ContestControllerUtils.getInstance().setContestService(contestService);
    }

    private void buildControllers() {
        controllersRegistry = ImmutableMap.<Class<?>, Controller> builder()
                .put(ApplicationController.class, new ApplicationController(userService))
                .put(JophielClientController.class, new JophielClientController(userService))
                .put(ContestAnnouncementController.class, new ContestAnnouncementController(contestService))
                .put(ContestClarificationController.class, new ContestClarificationController(contestService))
                .put(ContestContestantController.class, new ContestContestantController(contestService, userService))
                .put(ContestController.class, new ContestController(contestService))
                .put(ContestManagerController.class, new ContestManagerController(contestService, userService))
                .put(ContestProblemController.class, new ContestProblemController(contestService, submissionService))
                .put(ContestScoreboardController.class, new ContestScoreboardController(contestService, submissionService))
                .put(ContestSubmissionController.class, new ContestSubmissionController(contestService, submissionService, submissionLocalFileProvider, submissionRemoteFileProvider))
                .put(ContestSupervisorController.class, new ContestSupervisorController(contestService, userService))
                .put(ContestTeamController.class, new ContestTeamController(contestService, userService))
                .put(UserController.class, new UserController(userService))
                .put(ContestAPIController.class, new ContestAPIController(contestService))
                .put(ContestTestingAPIController.class, new ContestTestingAPIController(contestService, submissionService, submissionLocalFileProvider))
                .build();
    }

    private void scheduleThreads() {
        GradingResponsePoller poller = new GradingResponsePoller(submissionService, sealtiel, TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        ScoreUpdater updater = new ScoreUpdater(contestService, submissionService);
        UserActivityPusher userActivityPusher = new UserActivityPusher(userService, UserActivityServiceImpl.getInstance());

        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), updater, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityPusher, context);
    }
}
