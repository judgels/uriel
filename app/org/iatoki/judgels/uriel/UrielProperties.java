package org.iatoki.judgels.uriel;

import com.amazonaws.services.s3.model.Region;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import play.Configuration;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class UrielProperties {
    private static UrielProperties INSTANCE;

    private File submissionDir;
    private File teamAvatarDir;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsTeamAvatarBucketName;
    private Region awsTeamAvatarRegion;
    private String awsTeamAvatarCloudFrontURL;
    private String awsSubmissionBucketName;
    private Region awsSubmissionRegion;
    private boolean usingAWS;

    private UrielProperties() {

    }

    public File getSubmissionDir() {
        return this.submissionDir;
    }

    public File getTeamAvatarDir() {
        return teamAvatarDir;
    }

    public String getAWSAccessKey() {
        if ((usingAWS) && (Play.isDev())) {
            return awsAccessKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAWSSecretKey() {
        if ((usingAWS) && (Play.isDev())) {
            return awsSecretKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAWSTeamAvatarBucketName() {
        if (usingAWS) {
            return awsTeamAvatarBucketName;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Region getAWSTeamAvatarRegion() {
        if (usingAWS) {
            return awsTeamAvatarRegion;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAWSTeamAvatarCloudFrontURL() {
        if (usingAWS) {
            return awsTeamAvatarCloudFrontURL;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAWSSubmissionBucketName() {
        if (usingAWS) {
            return awsSubmissionBucketName;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Region getAWSSubmissionRegion() {
        if (usingAWS) {
            return awsSubmissionRegion;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isUsingAWS() {
        return usingAWS;
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UrielProperties();

            Configuration conf = Play.application().configuration();

            if (Play.isProd()) {
                verifyConfigurationProd(conf);
            } else if (Play.isDev()) {
                verifyConfigurationDev(conf);

                if (INSTANCE.isUsingAWS()) {
                    INSTANCE.awsAccessKey = conf.getString("aws.access.key");
                    INSTANCE.awsSecretKey = conf.getString("aws.secret.key");
                }
            }

            String baseDirName = conf.getString("uriel.baseDataDir");

            if (INSTANCE.isUsingAWS()) {
                INSTANCE.awsTeamAvatarBucketName = conf.getString("aws.team.avatar.bucket.name");
                INSTANCE.awsTeamAvatarRegion = Region.fromValue(conf.getString("aws.team.avatar.bucket.region.id"));
                INSTANCE.awsTeamAvatarCloudFrontURL = conf.getString("aws.team.avatar.cloudfront.url");
                INSTANCE.awsSubmissionBucketName = conf.getString("aws.submission.bucket.name");
                INSTANCE.awsSubmissionRegion = Region.fromValue(conf.getString("aws.submission.bucket.region.id"));
            }

            File baseDir = new File(baseDirName);
            if (!baseDir.isDirectory()) {
                throw new RuntimeException("uriel.baseDataDir: " + baseDirName + " does not exist");
            }

            try {
                INSTANCE.submissionDir = new File(baseDir, "submission");
                FileUtils.forceMkdir(INSTANCE.submissionDir);
                INSTANCE.teamAvatarDir = new File(baseDir, "teamAvatar");
                FileUtils.forceMkdir(INSTANCE.teamAvatarDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create folder inside " + baseDir.getAbsolutePath());
            }
        }
        return INSTANCE;
    }

    private static void verifyConfigurationDev(Configuration configuration) {
        List<String> requiredKeys = Lists.newArrayList(
              "jophiel.baseUrl",
              "jophiel.clientJid",
              "jophiel.clientSecret",
              "sealtiel.baseUrl",
              "sealtiel.clientJid",
              "sealtiel.clientSecret",
              "sealtiel.gabrielClientJid",
              "uriel.baseUrl",
              "uriel.baseDataDir",
              "sandalphon.baseUrl",
              "aws.use"
        );

        INSTANCE.usingAWS = false;
        if ((configuration.getBoolean("aws.use") != null) && ((configuration.getBoolean("aws.use")))) {
            INSTANCE.usingAWS = true;
            requiredKeys.add("aws.access.key");
            requiredKeys.add("aws.secret.key");
            requiredKeys.add("aws.team.avatar.bucket.name");
            requiredKeys.add("aws.team.avatar.bucket.region.id");
            requiredKeys.add("aws.submission.bucket.name");
            requiredKeys.add("aws.submission.bucket.region.id");
        }

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }

    private static void verifyConfigurationProd(Configuration configuration) {
        List<String> requiredKeys = Lists.newArrayList(
              "jophiel.baseUrl",
              "jophiel.clientJid",
              "jophiel.clientSecret",
              "sealtiel.baseUrl",
              "sealtiel.clientJid",
              "sealtiel.clientSecret",
              "sealtiel.gabrielClientJid",
              "uriel.baseUrl",
              "uriel.baseDataDir",
              "sandalphon.baseUrl",
              "aws.use"
        );

        INSTANCE.usingAWS = false;
        if ((configuration.getBoolean("aws.use") != null) && ((configuration.getBoolean("aws.use")))) {
            INSTANCE.usingAWS = true;
            requiredKeys.add("aws.team.avatar.bucket.name");
            requiredKeys.add("aws.team.avatar.bucket.region.id");
            requiredKeys.add("aws.submission.bucket.name");
            requiredKeys.add("aws.submission.bucket.region.id");
        }


        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
