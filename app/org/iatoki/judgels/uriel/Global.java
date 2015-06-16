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
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.controllers.JophielClientController;
import org.iatoki.judgels.jophiel.services.impls.DefaultUserActivityMessageServiceImpl;
import org.iatoki.judgels.sandalphon.commons.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import org.iatoki.judgels.sandalphon.commons.SubmissionService;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.controllers.ApplicationController;
import org.iatoki.judgels.uriel.controllers.ContestAnnouncementController;
import org.iatoki.judgels.uriel.controllers.ContestClarificationController;
import org.iatoki.judgels.uriel.controllers.ContestContestantController;
import org.iatoki.judgels.uriel.controllers.ContestController;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.ContestFileController;
import org.iatoki.judgels.uriel.controllers.ContestManagerController;
import org.iatoki.judgels.uriel.controllers.ContestProblemController;
import org.iatoki.judgels.uriel.controllers.ContestScoreboardController;
import org.iatoki.judgels.uriel.controllers.ContestSubmissionController;
import org.iatoki.judgels.uriel.controllers.ContestSupervisorController;
import org.iatoki.judgels.uriel.controllers.ContestTeamController;
import org.iatoki.judgels.uriel.controllers.ControllerUtils;
import org.iatoki.judgels.uriel.controllers.UserController;
import org.iatoki.judgels.uriel.controllers.apis.ContestAPIController;
import org.iatoki.judgels.uriel.controllers.apis.ContestTestingAPIController;
import org.iatoki.judgels.uriel.models.daos.impls.AvatarCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestAnnouncementHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestClarificationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestConfigurationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestContestantHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestContestantPasswordHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestManagerHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestProblemHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestReadHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestScoreboardHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestSupervisorHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestTeamCoachHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestTeamHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.ContestTeamMemberHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.GradingHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.JidCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.SubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.impls.UserHibernateDao;
import org.iatoki.judgels.uriel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.uriel.models.daos.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.ContestConfigurationDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantPasswordDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.ContestReadDao;
import org.iatoki.judgels.uriel.models.daos.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.daos.GradingDao;
import org.iatoki.judgels.uriel.models.daos.JidCacheDao;
import org.iatoki.judgels.uriel.models.daos.SubmissionDao;
import org.iatoki.judgels.uriel.models.daos.UserDao;
import org.iatoki.judgels.uriel.services.AvatarCacheService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.impls.ContestServiceImpl;
import org.iatoki.judgels.uriel.services.impls.JidCacheService;
import org.iatoki.judgels.uriel.services.impls.SubmissionServiceImpl;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.uriel.services.impls.UserServiceImpl;
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
    private ContestContestantPasswordDao contestContestantPasswordDao;
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

    private Jophiel jophiel;
    private Sandalphon sandalphon;
    private Sealtiel sealtiel;

    private FileSystemProvider teamAvatarFileProvider;
    private FileSystemProvider submissionLocalFileProvider;
    private FileSystemProvider submissionRemoteFileProvider;
    private FileSystemProvider contestFileProvider;

    private ContestService contestService;
    private SubmissionService submissionService;
    private UserService userService;

    private Map<Class<?>, Controller> controllersRegistry;

    @Override
    public void onStart(Application application) {
        buildProperties();
        buildDaos();
        buildCommons();
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
        contestContestantPasswordDao = new ContestContestantPasswordHibernateDao();
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

    private void buildCommons() {
        jophiel = new Jophiel(urielProps.getJophielBaseUrl(), urielProps.getJophielClientJid(), urielProps.getJophielClientSecret());
        sandalphon = new Sandalphon(urielProps.getSandalphonBaseUrl(), urielProps.getSandalphonClientJid(), urielProps.getSandalphonClientSecret());
        sealtiel = new Sealtiel(urielProps.getSealtielBaseUrl(), urielProps.getSealtielClientJid(), urielProps.getSealtielClientSecret());
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

        if (urielProps.isFileUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProps.isFileAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProps.getFileAWSAccessKey(), urielProps.getFileAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            contestFileProvider = new AWSFileSystemProvider(awsS3Client, urielProps.getFileAWSS3BucketName(), urielProps.getFileAWSS3BucketRegion());
        } else {
            contestFileProvider = new LocalFileSystemProvider(urielProps.getFileLocalDir());
        }
    }

    private void buildServices() {
        contestService = new ContestServiceImpl(
                contestDao,
                contestAnnouncementDao,
                contestProblemDao,
                contestClarificationDao,
                contestContestantDao,
                contestContestantPasswordDao,
                contestTeamDao,
                contestTeamCoachDao,
                contestTeamMemberDao,
                contestSupervisorDao,
                contestManagerDao,
                contestScoreboardDao,
                contestConfigurationDao,
                contestReadDao,
                teamAvatarFileProvider,
                contestFileProvider
        );

        submissionService = new SubmissionServiceImpl(submissionDao, gradingDao, sealtiel, urielProps.getSealtielGabrielClientJid());
        userService = new UserServiceImpl(jophiel, userDao);

        JidCacheService.buildInstance(jidCacheDao);
        AvatarCacheService.buildInstance(jophiel, avatarCacheDao);
        ControllerUtils.buildInstance(jophiel);
        DefaultUserActivityMessageServiceImpl.buildInstance(jophiel);
    }

    private void buildUtils() {
        ContestControllerUtils.getInstance().setContestService(contestService);
    }

    private void buildControllers() {
        controllersRegistry = ImmutableMap.<Class<?>, Controller> builder()
                .put(ApplicationController.class, new ApplicationController(jophiel, userService))
                .put(JophielClientController.class, new JophielClientController(jophiel, userService))
                .put(ContestAnnouncementController.class, new ContestAnnouncementController(contestService))
                .put(ContestClarificationController.class, new ContestClarificationController(contestService))
                .put(ContestContestantController.class, new ContestContestantController(jophiel, contestService, userService))
                .put(ContestController.class, new ContestController(contestService))
                .put(ContestManagerController.class, new ContestManagerController(jophiel, contestService, userService))
                .put(ContestProblemController.class, new ContestProblemController(sandalphon, contestService, submissionService))
                .put(ContestScoreboardController.class, new ContestScoreboardController(contestService, submissionService))
                .put(ContestSubmissionController.class, new ContestSubmissionController(contestService, submissionService, submissionLocalFileProvider, submissionRemoteFileProvider))
                .put(ContestSupervisorController.class, new ContestSupervisorController(jophiel, contestService, userService))
                .put(ContestTeamController.class, new ContestTeamController(jophiel, contestService, userService))
                .put(ContestFileController.class, new ContestFileController(contestService))
                .put(UserController.class, new UserController(jophiel, userService))
                .put(ContestAPIController.class, new ContestAPIController(contestService))
                .put(ContestTestingAPIController.class, new ContestTestingAPIController(contestService, submissionService, submissionLocalFileProvider))
                .build();
    }

    private void scheduleThreads() {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, submissionService, sealtiel, TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        ScoreUpdater updater = new ScoreUpdater(contestService, submissionService);
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(jophiel, userService, UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), updater, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
