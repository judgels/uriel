package org.iatoki.judgels.uriel;

import com.amazonaws.services.s3.model.Region;
import com.google.common.collect.ImmutableList;
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
    private String teamAvatarBucketName;
    private Region teamAvatarRegion;

    private UrielProperties() {

    }

    public File getSubmissionDir() {
        return this.submissionDir;
    }

    public File getTeamAvatarDir() {
        return teamAvatarDir;
    }

    public String getaWSAccessKey() {
        return aWSAccessKey;
    }

    public String getaWSSecretKey() {
        return aWSSecretKey;
    }

    public String getTeamAvatarBucketName() {
        return teamAvatarBucketName;
    }

    public Region getTeamAvatarRegion() {
        return teamAvatarRegion;
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UrielProperties();

            Configuration conf = Play.application().configuration();

            if (Play.isProd()) {
                verifyConfigurationProd(conf);
            } else if (Play.isDev()) {
                verifyConfigurationDev(conf);

                INSTANCE.aWSAccessKey = conf.getString("aws.access.key");
                INSTANCE.aWSSecretKey = conf.getString("aws.secret.key");
            }

            String baseDirName = conf.getString("uriel.baseDataDir");

            INSTANCE.teamAvatarBucketName = conf.getString("aws.team.avatar.bucket.name");
            INSTANCE.teamAvatarRegion = Region.fromValue(conf.getString("aws.team.avatar.bucket.region.id"));

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
        List<String> requiredKeys = ImmutableList.of(
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
              "aws.access.key",
              "aws.secret.key",
              "aws.team.avatar.bucket.name",
              "aws.team.avatar.bucket.region.id"
        );

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }

    private static void verifyConfigurationProd(Configuration configuration) {
        List<String> requiredKeys = ImmutableList.of(
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
              "aws.team.avatar.bucket.name",
              "aws.team.avatar.bucket.region.id"
        );

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
