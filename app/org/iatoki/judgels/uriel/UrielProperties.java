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
    private String aWSAccessKey;
    private String aWSSecretKey;
    private String aWSTeamAvatarBucketName;
    private Region aWSTeamAvatarRegion;
    private String aWSTeamAvatarCloudFrontURL;
    private String aWSSubmissionBucketName;
    private Region aWSSubmissionRegion;
    private boolean useAWS;

    private UrielProperties() {

    }

    public File getSubmissionDir() {
        return this.submissionDir;
    }

    public File getTeamAvatarDir() {
        return teamAvatarDir;
    }

    public String getaWSAccessKey() {
        if ((useAWS) && (Play.isDev())) {
            return aWSAccessKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getaWSSecretKey() {
        if ((useAWS) && (Play.isDev())) {
            return aWSSecretKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getaWSTeamAvatarBucketName() {
        if (useAWS) {
            return aWSTeamAvatarBucketName;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Region getaWSTeamAvatarRegion() {
        if (useAWS) {
            return aWSTeamAvatarRegion;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getaWSTeamAvatarCloudFrontURL() {
        if (useAWS) {
            return aWSTeamAvatarCloudFrontURL;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getaWSSubmissionBucketName() {
        if (useAWS) {
            return aWSSubmissionBucketName;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Region getaWSSubmissionRegion() {
        if (useAWS) {
            return aWSSubmissionRegion;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isUseAWS() {
        return useAWS;
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UrielProperties();

            Configuration conf = Play.application().configuration();

            if (Play.isProd()) {
                verifyConfigurationProd(conf);
            } else if (Play.isDev()) {
                verifyConfigurationDev(conf);

                if (INSTANCE.isUseAWS()) {
                    INSTANCE.aWSAccessKey = conf.getString("aws.access.key");
                    INSTANCE.aWSSecretKey = conf.getString("aws.secret.key");
                }
            }

            String baseDirName = conf.getString("uriel.baseDataDir");

            if (INSTANCE.isUseAWS()) {
                INSTANCE.aWSTeamAvatarBucketName = conf.getString("aws.team.avatar.bucket.name");
                INSTANCE.aWSTeamAvatarRegion = Region.fromValue(conf.getString("aws.team.avatar.bucket.region.id"));
                INSTANCE.aWSTeamAvatarCloudFrontURL = conf.getString("aws.team.avatar.cloudfront.url");
                INSTANCE.aWSSubmissionBucketName = conf.getString("aws.submission.bucket.name");
                INSTANCE.aWSSubmissionRegion = Region.fromValue(conf.getString("aws.submission.bucket.region.id"));
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

        INSTANCE.useAWS = false;
        if ((configuration.getBoolean("aws.use") != null) && ((configuration.getBoolean("aws.use")))) {
            INSTANCE.useAWS = true;
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

        INSTANCE.useAWS = false;
        if ((configuration.getBoolean("aws.use") != null) && ((configuration.getBoolean("aws.use")))) {
            INSTANCE.useAWS = true;
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
