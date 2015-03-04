package org.iatoki.judgels.uriel;

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

    private UrielProperties() {

    }

    public File getSubmissionDir() {
        return this.submissionDir;
    }

    public File getTeamAvatarDir() {
        return teamAvatarDir;
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UrielProperties();

            Configuration conf = Play.application().configuration();

            verifyConfiguration(conf);

            String baseDirName = conf.getString("uriel.baseDataDir").replaceAll("\"", "");

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

    private static void verifyConfiguration(Configuration configuration) {
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
                "sandalphon.baseUrl"
        );

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
