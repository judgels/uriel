package org.iatoki.judgels.uriel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.UrielProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.iatoki.judgels.uriel.models.daos",
        "org.iatoki.judgels.uriel.services",
})
public class PersistenceConfig {

    @Bean
    public JudgelsProperties judgelsProperties() {
        org.iatoki.judgels.uriel.BuildInfo$ buildInfo = org.iatoki.judgels.uriel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());
        return JudgelsProperties.getInstance();
    }

    @Bean
    public UrielProperties urielProperties() {
        Config config = ConfigFactory.load();
        UrielProperties.buildInstance(config);
        return UrielProperties.getInstance();
    }

    @Bean
    public Jophiel jophiel() {
        return new Jophiel(urielProperties().getJophielBaseUrl(), urielProperties().getJophielClientJid(), urielProperties().getJophielClientSecret());
    }

    @Bean
    public Sandalphon sandalphon() {
        return new Sandalphon(urielProperties().getSandalphonBaseUrl(), urielProperties().getSandalphonClientJid(), urielProperties().getSandalphonClientSecret());
    }

    @Bean
    public Sealtiel sealtiel() {
        return new Sealtiel(urielProperties().getSealtielBaseUrl(), urielProperties().getSealtielClientJid(), urielProperties().getSealtielClientSecret());
    }

    @Bean
    public FileSystemProvider teamAvatarFileSystemProvider() {
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

    @Bean
    public FileSystemProvider submissionRemoteFileSystemProvider() {
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

    @Bean
    public FileSystemProvider submissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(urielProperties().getSubmissionLocalDir());
    }

    @Bean
    public FileSystemProvider contestFileSystemProvider() {
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

    @Bean
    public String gabrielClientJid() {
        return urielProperties().getSealtielGabrielClientJid();
    }
}
