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

    private UrielProperties() {

    }

    public File getSubmissionDir() {
        return this.submissionDir;
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UrielProperties();

            Configuration conf = Play.application().configuration();

            verifyConfiguration(conf);

            String baseDirName = conf.getString("uriel.baseDir").replaceAll("\"", "");

            File baseDir = new File(baseDirName);
            if (!baseDir.isDirectory()) {
                throw new RuntimeException("uriel.baseDir: " + baseDirName + " does not exist");
            }

            try {
                INSTANCE.submissionDir = new File(baseDir, "submission");
                FileUtils.forceMkdir(INSTANCE.submissionDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create folder inside " + baseDir.getAbsolutePath());
            }
        }
        return INSTANCE;
    }

    private static void verifyConfiguration(Configuration configuration) {
        List<String> requiredKeys = ImmutableList.of(
                "jophiel.baseUrl",
                "jophiel.clientId",
                "jophiel.clientSecret",
                "sealtiel.baseUrl",
                "sealtiel.clientChannel",
                "sealtiel.clientId",
                "sealtiel.clientSecret",
                "uriel.baseDir",
                "sandalphon.baseUrl"
        );

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
