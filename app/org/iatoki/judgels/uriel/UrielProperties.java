package org.iatoki.judgels.uriel;

import com.amazonaws.services.s3.model.Region;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class UrielProperties {
    private static UrielProperties INSTANCE;

    private final Config config;

    private String urielBaseUrl;
    private File urielBaseDataDir;
    private String urielStressTestSecret;
    private String urielScoreboardSecret;

    private String jophielBaseUrl;
    private String jophielClientJid;
    private String jophielClientSecret;

    private String sandalphonBaseUrl;
    private String sandalphonClientJid;
    private String sandalphonClientSecret;

    private String sealtielBaseUrl;
    private String sealtielClientJid;
    private String sealtielClientSecret;
    private String sealtielGabrielClientJid;

    private Boolean globalAWSUsingKeys;
    private String globalAWSAccessKey;
    private String globalAWSSecretKey;
    private Region globalAWSS3Region;

    private boolean teamAvatarUsingAWSS3;
    private File teamAvatarLocalDir;
    private Boolean teamAvatarAWSUsingKeys;
    private String teamAvatarAWSAccessKey;
    private String teamAvatarAWSSecretKey;
    private String teamAvatarAWSS3BucketName;
    private Region teamAvatarAWSS3BucketRegion;
    private String teamAvatarAWSCloudFrontUrl;

    private boolean submissionUsingAWSS3;
    private Boolean submissionAWSUsingKeys;
    private File submissionLocalDir;
    private String submissionAWSAccessKey;
    private String submissionAWSSecretKey;
    private String submissionAWSS3BucketName;
    private Region submissionAWSS3BucketRegion;

    private boolean fileUsingAWSS3;
    private Boolean fileAWSUsingKeys;
    private File fileLocalDir;
    private String fileAWSAccessKey;
    private String fileAWSSecretKey;
    private String fileAWSS3BucketName;
    private Region fileAWSS3BucketRegion;

    private Set<String> criticalContestJids;

    private UrielProperties(Config config) {
        this.config = config;
    }

    public static synchronized void buildInstance(Config config) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("UrielProperties instance has already been built");
        }

        INSTANCE = new UrielProperties(config);
        INSTANCE.build();
    }

    public static UrielProperties getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("UrielProperties instance has not been built");
        }
        return INSTANCE;
    }

    public String getUrielBaseUrl() {
        return urielBaseUrl;
    }

    public String getUrielStressTestSecret() {
        return urielStressTestSecret;
    }

    public String getUrielScoreboardSecret() {
        return urielScoreboardSecret;
    }

    public String getJophielBaseUrl() {
        return jophielBaseUrl;
    }

    public String getJophielClientJid() {
        return jophielClientJid;
    }

    public String getJophielClientSecret() {
        return jophielClientSecret;
    }

    public String getSandalphonBaseUrl() {
        return sandalphonBaseUrl;
    }

    public String getSandalphonClientJid() {
        return sandalphonClientJid;
    }

    public String getSandalphonClientSecret() {
        return sandalphonClientSecret;
    }

    public String getSealtielBaseUrl() {
        return sealtielBaseUrl;
    }

    public String getSealtielClientJid() {
        return sealtielClientJid;
    }

    public String getSealtielClientSecret() {
        return sealtielClientSecret;
    }

    public String getSealtielGabrielClientJid() {
        return sealtielGabrielClientJid;
    }

    public boolean isTeamAvatarUsingAWSS3() {
        return teamAvatarUsingAWSS3;
    }

    public File getTeamAvatarLocalDir() {
        if (isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is using AWS S3");
        }
        return teamAvatarLocalDir;
    }

    public String getTeamAvatarAWSAccessKey() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }

        if (teamAvatarAWSAccessKey != null) {
            return teamAvatarAWSAccessKey;
        }

        if (globalAWSAccessKey != null) {
            return globalAWSAccessKey;
        }

        throw new RuntimeException("Missing aws.global.key.access or aws.teamAvatar.key.access");
    }

    public String getTeamAvatarAWSSecretKey() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }

        if (teamAvatarAWSSecretKey != null) {
            return teamAvatarAWSSecretKey;
        }

        if (globalAWSSecretKey != null) {
            return globalAWSSecretKey;
        }

        throw new RuntimeException("Missing aws.global.key.secret or aws.teamAvatar.key.secret");
    }

    public String getTeamAvatarAWSS3BucketName() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }
        return teamAvatarAWSS3BucketName;
    }

    public Region getTeamAvatarAWSS3BucketRegion() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }
        if (teamAvatarAWSS3BucketRegion != null) {
            return teamAvatarAWSS3BucketRegion;
        }
        if (globalAWSS3Region != null) {
            return globalAWSS3Region;
        }

        throw new RuntimeException("Missing aws.global.s3.bucket.regionId or aws.teamAvatar.s3.bucket.regionId in");
    }

    public String getTeamAvatarAWSCloudFrontUrl() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }
        return teamAvatarAWSCloudFrontUrl;
    }

    public boolean isTeamAvatarAWSUsingKeys() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }
        if (teamAvatarAWSUsingKeys != null) {
            return teamAvatarAWSUsingKeys;
        }
        if (globalAWSUsingKeys != null) {
            return globalAWSUsingKeys;
        }

        throw new RuntimeException("Missing aws.global.key.use or aws.teamAvatar.key.use");
    }

    public boolean isSubmissionUsingAWSS3() {
        return submissionUsingAWSS3;
    }

    public File getSubmissionLocalDir() {
        return submissionLocalDir;
    }

    public String getSubmissionAWSAccessKey() {
        if (!isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }
        if (submissionAWSAccessKey != null) {
            return submissionAWSAccessKey;
        }
        if (globalAWSAccessKey != null) {
            return globalAWSAccessKey;
        }

        throw new RuntimeException("Missing aws.global.key.access or aws.submission.key.access");
    }

    public String getSubmissionAWSSecretKey() {
        if (!isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }
        if (submissionAWSSecretKey != null) {
            return submissionAWSSecretKey;
        }
        if (globalAWSSecretKey != null) {
            return globalAWSSecretKey;
        }

        throw new RuntimeException("Missing aws.global.key.secret or aws.submission.key.secret in");
    }

    public String getSubmissionAWSS3BucketName() {
        if (!isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }
        return submissionAWSS3BucketName;
    }

    public Region getSubmissionAWSS3BucketRegion() {
        if (!isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }
        if (submissionAWSS3BucketRegion != null) {
            return submissionAWSS3BucketRegion;
        }
        if (globalAWSS3Region != null) {
            return globalAWSS3Region;
        }

        throw new RuntimeException("Missing aws.global.s3.bucket.regionId or aws.submission.s3.bucket.regionId");
    }

    public boolean isSubmissionAWSUsingKeys() {
        if (!isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }

        if (submissionAWSUsingKeys != null) {
            return submissionAWSUsingKeys;
        }
        if (globalAWSUsingKeys != null) {
            return globalAWSUsingKeys;
        }

        throw new RuntimeException("Missing aws.global.key.use or aws.submission.key.use in");
    }

    public boolean isFileUsingAWSS3() {
        return fileUsingAWSS3;
    }

    public File getFileLocalDir() {
        return fileLocalDir;
    }

    public String getFileAWSAccessKey() {
        if (!isFileUsingAWSS3()) {
            throw new UnsupportedOperationException("File is not using AWS S3");
        }
        if (fileAWSAccessKey != null) {
            return fileAWSAccessKey;
        }
        if (globalAWSAccessKey != null) {
            return globalAWSAccessKey;
        }

        throw new RuntimeException("Missing aws.global.key.access or aws.submission.key.access");
    }

    public String getFileAWSSecretKey() {
        if (!isFileUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }
        if (fileAWSUsingKeys != null) {
            return fileAWSSecretKey;
        }
        if (globalAWSSecretKey != null) {
            return globalAWSSecretKey;
        }

        throw new RuntimeException("Missing aws.global.key.secret or aws.aws.key.secret in");
    }

    public String getFileAWSS3BucketName() {
        if (!isFileUsingAWSS3()) {
            throw new UnsupportedOperationException("File is not using AWS S3");
        }
        return fileAWSS3BucketName;
    }

    public Region getFileAWSS3BucketRegion() {
        if (!isFileUsingAWSS3()) {
            throw new UnsupportedOperationException("File is not using AWS S3");
        }
        if (fileAWSS3BucketRegion != null) {
            return fileAWSS3BucketRegion;
        }
        if (globalAWSS3Region != null) {
            return globalAWSS3Region;
        }

        throw new RuntimeException("Missing aws.global.s3.bucket.regionId or aws.file.s3.bucket.regionId");
    }

    public boolean isFileAWSUsingKeys() {
        if (!isFileUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is not using AWS S3");
        }

        if (fileAWSUsingKeys != null) {
            return fileAWSUsingKeys;
        }
        if (globalAWSUsingKeys != null) {
            return globalAWSUsingKeys;
        }

        throw new RuntimeException("Missing aws.global.key.use or aws.file.key.use in");
    }

    public boolean isContestCritial(String contestJid) {
        return criticalContestJids.contains(contestJid);
    }

    private void build() {
        urielBaseUrl = requireStringValue("uriel.baseUrl");
        urielBaseDataDir = requireDirectoryValue("uriel.baseDataDir");
        urielStressTestSecret = getStringValue("uriel.stressTestSecret");
        urielScoreboardSecret = getStringValue("uriel.scoreboardSecret");

        jophielBaseUrl = requireStringValue("jophiel.baseUrl");
        jophielClientJid = requireStringValue("jophiel.clientJid");
        jophielClientSecret = requireStringValue("jophiel.clientSecret");

        sandalphonBaseUrl = requireStringValue("sandalphon.baseUrl");
        sandalphonClientJid = requireStringValue("sandalphon.clientJid");
        sandalphonClientSecret = requireStringValue("sandalphon.clientSecret");

        sealtielBaseUrl = requireStringValue("sealtiel.baseUrl");
        sealtielClientJid = requireStringValue("sealtiel.clientJid");
        sealtielClientSecret = requireStringValue("sealtiel.clientSecret");
        sealtielGabrielClientJid = requireStringValue("sealtiel.gabrielClientJid");

        globalAWSUsingKeys = getBooleanValue("aws.global.key.use");
        globalAWSAccessKey = getStringValue("aws.global.key.access");
        globalAWSSecretKey = getStringValue("aws.global.key.secret");
        globalAWSS3Region = Region.fromValue(getStringValue("aws.global.s3.bucket.regionId"));

        teamAvatarUsingAWSS3 = requireBooleanValue("aws.teamAvatar.s3.use");
        teamAvatarAWSUsingKeys = getBooleanValue("aws.teamAvatar.key.use");
        teamAvatarAWSAccessKey = getStringValue("aws.teamAvatar.key.access");
        teamAvatarAWSSecretKey = getStringValue("aws.teamAvatar.key.secret");
        teamAvatarAWSS3BucketName = getStringValue("aws.teamAvatar.s3.bucket.name");
        teamAvatarAWSS3BucketRegion = Region.fromValue(getStringValue("aws.teamAvatar.s3.bucket.regionId"));

        if (teamAvatarUsingAWSS3) {
            teamAvatarAWSCloudFrontUrl = requireStringValue("aws.teamAvatar.cloudFront.baseUrl");
        } else {
            try {
                teamAvatarLocalDir = new File(urielBaseDataDir, "teamAvatar");
                FileUtils.forceMkdir(teamAvatarLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        submissionUsingAWSS3 = requireBooleanValue("aws.submission.s3.use");
        submissionAWSUsingKeys = getBooleanValue("aws.submission.key.use");
        submissionAWSAccessKey = getStringValue("aws.submission.key.access");
        submissionAWSSecretKey = getStringValue("aws.submission.key.secret");
        submissionAWSS3BucketName = getStringValue("aws.submission.s3.bucket.name");
        submissionAWSS3BucketRegion = Region.fromValue(getStringValue("aws.submission.s3.bucket.regionId"));

        try {
            submissionLocalDir = new File(urielBaseDataDir, "submission");
            FileUtils.forceMkdir(submissionLocalDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileUsingAWSS3 = requireBooleanValue("aws.file.s3.use");
        fileAWSUsingKeys = getBooleanValue("aws.file.key.use");
        fileAWSAccessKey = getStringValue("aws.file.key.access");
        fileAWSSecretKey = getStringValue("aws.file.key.secret");
        fileAWSS3BucketName = getStringValue("aws.file.s3.bucket.name");
        fileAWSS3BucketRegion = Region.fromValue(getStringValue("aws.file.s3.bucket.regionId"));

        if (!fileUsingAWSS3) {
            try {
                fileLocalDir = new File(urielBaseDataDir, "file");
                FileUtils.forceMkdir(fileLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String criticalContestJidsAsString = getStringValue("uriel.criticalContestJids");
        if (criticalContestJidsAsString != null) {
            criticalContestJids = ImmutableSet.copyOf(Sets.newHashSet(criticalContestJidsAsString.split(",")));
        } else {
            criticalContestJids = ImmutableSet.of();
        }
    }

    private String getStringValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getString(key);
    }

    private String requireStringValue(String key) {
        return config.getString(key);
    }

    private Boolean getBooleanValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getBoolean(key);
    }

    private boolean requireBooleanValue(String key) {
        return config.getBoolean(key);
    }

    private File requireDirectoryValue(String key) {
        String filename = config.getString(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
        return dir;
    }
}
