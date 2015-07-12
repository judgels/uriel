package org.iatoki.judgels.uriel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.util.Providers;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.play.config.AbstractJudgelsModule;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.services.impls.UserServiceImpl;

public final class UrielModule extends AbstractJudgelsModule {

    @Override
    protected void manualBinding() {
        bind(Jophiel.class).toInstance(jophiel());
        bind(Sandalphon.class).toInstance(sandalphon());
        bind(Sealtiel.class).toInstance(sealtiel());
        bind(FileSystemProvider.class).annotatedWith(ContestFile.class).toInstance(contestFileSystemProvider());
        bind(FileSystemProvider.class).annotatedWith(TeamAvatarFile.class).toInstance(teamAvatarFileSystemProvider());
        bind(FileSystemProvider.class).annotatedWith(SubmissionLocalFile.class).toInstance(submissionLocalFileSystemProvider());

        FileSystemProvider submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider();
        if (submissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(SubmissionRemoteFile.class).toInstance(submissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(SubmissionRemoteFile.class).toProvider(Providers.of(null));
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

    private Jophiel jophiel() {
        return new Jophiel(urielProperties().getJophielBaseUrl(), urielProperties().getJophielClientJid(), urielProperties().getJophielClientSecret());
    }

    private Sandalphon sandalphon() {
        return new Sandalphon(urielProperties().getSandalphonBaseUrl(), urielProperties().getSandalphonClientJid(), urielProperties().getSandalphonClientSecret());
    }

    private Sealtiel sealtiel() {
        return new Sealtiel(urielProperties().getSealtielBaseUrl(), urielProperties().getSealtielClientJid(), urielProperties().getSealtielClientSecret());
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
