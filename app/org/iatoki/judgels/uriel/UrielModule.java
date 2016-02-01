package org.iatoki.judgels.uriel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.util.Providers;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielFactory;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonFactory;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.api.sealtiel.SealtielFactory;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.user.BaseUserService;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.play.general.GeneralName;
import org.iatoki.judgels.play.general.GeneralVersion;
import org.iatoki.judgels.play.migration.JudgelsDataMigrator;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.contest.file.ContestFileSystemProvider;
import org.iatoki.judgels.uriel.contest.submission.programming.GabrielClientJid;
import org.iatoki.judgels.uriel.contest.submission.programming.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.uriel.contest.submission.programming.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.uriel.contest.submission.programming.ProgrammingSubmissionServiceImpl;
import org.iatoki.judgels.uriel.contest.team.TeamAvatarFileSystemProvider;
import org.iatoki.judgels.uriel.user.UserServiceImpl;

public class UrielModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        org.iatoki.judgels.uriel.BuildInfo$ buildInfo = org.iatoki.judgels.uriel.BuildInfo$.MODULE$;

        bindConstant().annotatedWith(GeneralName.class).to(buildInfo.name());
        bindConstant().annotatedWith(GeneralVersion.class).to(buildInfo.version());

        // <DEPRECATED>
        Config config = ConfigFactory.load();
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), config);
        UrielProperties.buildInstance(config);
        bind(UrielSingletonsBuilder.class).asEagerSingleton();
        // </DEPRECATED>

        bind(JudgelsDataMigrator.class).to(UrielDataMigrator.class);

        bind(ProgrammingSubmissionService.class).to(ProgrammingSubmissionServiceImpl.class);

        bind(JophielAuthAPI.class).toInstance(jophielAuthAPI());
        bind(JophielClientAPI.class).toInstance(jophielClientAPI());
        bind(JophielPublicAPI.class).toInstance(jophielPublicAPI());
        bind(SandalphonClientAPI.class).toInstance(sandalphonClientAPI());
        bind(SealtielClientAPI.class).toInstance(sealtielClientAPI());
        bind(FileSystemProvider.class).annotatedWith(ContestFileSystemProvider.class).toInstance(contestFileSystemProvider());
        bind(FileSystemProvider.class).annotatedWith(TeamAvatarFileSystemProvider.class).toInstance(teamAvatarFileSystemProvider());
        bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionLocalFileSystemProvider.class).toInstance(submissionLocalFileSystemProvider());

        FileSystemProvider submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider();
        if (submissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionRemoteFileSystemProvider.class).toInstance(submissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionRemoteFileSystemProvider.class).toProvider(Providers.of(null));
        }

        bindConstant().annotatedWith(GabrielClientJid.class).to(gabrielClientJid());
        bind(BaseUserService.class).to(UserServiceImpl.class);
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.uriel.models.daos.hibernate";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.uriel.services.impls";
    }

    private UrielProperties urielProperties() {
        return UrielProperties.getInstance();
    }

    private JophielAuthAPI jophielAuthAPI() {
        return new JophielAuthAPI(urielProperties().getJophielBaseUrl(), urielProperties().getJophielClientJid(), urielProperties().getJophielClientSecret());
    }

    private JophielClientAPI jophielClientAPI() {
        return JophielFactory.createJophiel(urielProperties().getJophielBaseUrl()).connectToClientAPI(urielProperties().getJophielClientJid(), urielProperties().getJophielClientSecret());
    }

    private JophielPublicAPI jophielPublicAPI() {
        return JophielFactory.createJophiel(urielProperties().getJophielBaseUrl()).connectToPublicAPI();
    }

    private SandalphonClientAPI sandalphonClientAPI() {
        return SandalphonFactory.createSandalphon(urielProperties().getSandalphonBaseUrl()).connectToClientAPI(urielProperties().getSandalphonClientJid(), urielProperties().getSandalphonClientSecret());
    }

    private SealtielClientAPI sealtielClientAPI() {
        return SealtielFactory.createSealtiel(urielProperties().getSealtielBaseUrl()).connectToClientAPI(urielProperties().getSealtielClientJid(), urielProperties().getSealtielClientSecret());
    }

    private FileSystemProvider teamAvatarFileSystemProvider() {
        FileSystemProvider teamAvatarFileSystemProvider = null;
        if (urielProperties().isTeamAvatarUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProperties().isTeamAvatarAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProperties().getTeamAvatarAWSAccessKey(), urielProperties().getTeamAvatarAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            teamAvatarFileSystemProvider = new AWSFileSystemProvider(awsS3Client, urielProperties().getTeamAvatarAWSS3BucketName(), urielProperties().getTeamAvatarAWSCloudFrontUrl(), urielProperties().getTeamAvatarAWSS3BucketRegion());
        } else {
            teamAvatarFileSystemProvider = new LocalFileSystemProvider(urielProperties().getTeamAvatarLocalDir());
        }

        return teamAvatarFileSystemProvider;
    }

    private FileSystemProvider submissionRemoteFileSystemProvider() {
        FileSystemProvider submissionRemoteFileSystemProvider = null;
        if (urielProperties().isSubmissionUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProperties().isSubmissionAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProperties().getSubmissionAWSAccessKey(), urielProperties().getSubmissionAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            submissionRemoteFileSystemProvider = new AWSFileSystemProvider(awsS3Client, urielProperties().getSubmissionAWSS3BucketName(), urielProperties().getSubmissionAWSS3BucketRegion());
        }

        return submissionRemoteFileSystemProvider;
    }

    private FileSystemProvider submissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(urielProperties().getSubmissionLocalDir());
    }

    private FileSystemProvider contestFileSystemProvider() {
        FileSystemProvider contestFileSystemProvider = null;
        if (urielProperties().isFileUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (urielProperties().isFileAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(urielProperties().getFileAWSAccessKey(), urielProperties().getFileAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            contestFileSystemProvider = new AWSFileSystemProvider(awsS3Client, urielProperties().getFileAWSS3BucketName(), urielProperties().getFileAWSS3BucketRegion());
        } else {
            contestFileSystemProvider = new LocalFileSystemProvider(urielProperties().getFileLocalDir());
        }

        return contestFileSystemProvider;
    }

    private String gabrielClientJid() {
        return urielProperties().getSealtielGabrielClientJid();
    }
}
