package org.iatoki.judgels.uriel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.util.Providers;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielFactory;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.api.sealtiel.SealtielFactory;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.services.impls.UserServiceImpl;

public final class UrielModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        bind(JophielAuthAPI.class).toInstance(jophielAuthAPI());
        bind(JophielClientAPI.class).toInstance(jophielClientAPI());
        bind(JophielPublicAPI.class).toInstance(jophielPublicAPI());
        bind(Sandalphon.class).toInstance(sandalphon());
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
        return "org.iatoki.judgels.uriel.models.daos.impls";
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

    private Sandalphon sandalphon() {
        return new Sandalphon(urielProperties().getSandalphonBaseUrl(), urielProperties().getSandalphonClientJid(), urielProperties().getSandalphonClientSecret());
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
