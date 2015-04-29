package org.iatoki.judgels.uriel;

import com.amazonaws.services.s3.model.Region;
import org.apache.commons.io.FileUtils;
import play.Configuration;

import java.io.File;
import java.io.IOException;

public final class UrielProperties {
    private static UrielProperties INSTANCE;

    private final Configuration conf;
    private final String confLocation;

    private String urielBaseUrl;
    private File urielBaseDataDir;
    private String urielStressTestSecret;

    private String jophielBaseUrl;
    private String jophielClientJid;
    private String jophielClientSecret;

    private String sandalphonBaseUrl;

    private String sealtielBaseUrl;
    private String sealtielClientJid;
    private String sealtielClientSecret;
    private String sealtielGabrielClientJid;

    private String globalAWSAccessKey;
    private String globalAWSSecretKey;
    private Region globalAWSS3Region;
    private Boolean globalAWSS3PermittedByIAMRoles;

    private boolean teamAvatarAWSS3Use;
    private File teamAvatarLocalDir;
    private String teamAvatarAWSAccessKey;
    private String teamAvatarAWSSecretKey;
    private String teamAvatarAWSS3BucketName;
    private Region teamAvatarAWSS3BucketRegion;
    private Boolean teamAvatarAWSS3PermittedByIAMRoles;
    private String teamAvatarAWSCloudFrontUrl;

    private boolean submissionAWSS3Use;
    private File submissionLocalDir;
    private String submissionAWSAccessKey;
    private String submissionAWSSecretKey;
    private String submissionAWSS3BucketName;
    private Region submissionAWSS3BucketRegion;
    private Boolean submissionAWSS3PermittedByIAMRoles;

    private UrielProperties(Configuration conf, String confLocation) {
        this.conf = conf;
        this.confLocation = confLocation;
    }

    public static synchronized void buildInstance(Configuration conf, String confLocation) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("UrielProperties instance has already been built");
        }

        INSTANCE = new UrielProperties(conf, confLocation);
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
        return teamAvatarAWSS3Use;
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

        throw new RuntimeException("Missing aws.accessKey or teamAvatar.aws.accessKey in " + confLocation);
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

        throw new RuntimeException("Missing aws.secretKey or teamAvatar.aws.secretKey in " + confLocation);
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

        throw new RuntimeException("Missing aws.s3.bucket.regionId or teamAvatar.aws.s3.bucket.regionId in " + confLocation);
    }

    public String getTeamAvatarAWSCloudFrontUrl() {
        if (!isTeamAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Team avatar is not using AWS S3");
        }
        return teamAvatarAWSCloudFrontUrl;
    }

    public boolean isTeamAvatarAWSS3PermittedByIAMRoles() {
        if (teamAvatarAWSS3PermittedByIAMRoles != null) {
            return teamAvatarAWSS3PermittedByIAMRoles;
        }
        if (globalAWSS3PermittedByIAMRoles != null) {
            return globalAWSS3PermittedByIAMRoles;
        }

        throw new RuntimeException("Missing aws.s3.permittedByIAMRoles or teamAvatar.aws.s3.permittedByIAMRoles in " + confLocation);
    }

    public boolean isSubmissionUsingAWSS3() {
        return submissionAWSS3Use;
    }

    public File getSubmissionLocalDir() {
        if (isSubmissionUsingAWSS3()) {
            throw new UnsupportedOperationException("Submission is using AWS S3");
        }
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

        throw new RuntimeException("Missing aws.accessKey or submission.aws.accessKey in " + confLocation);
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

        throw new RuntimeException("Missing aws.secretKey or submission.aws.secretKey in " + confLocation);
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

        throw new RuntimeException("Missing aws.bucket.regionId or submission.aws.bucket.regionId in " + confLocation);
    }

    public boolean isSubmissionAWSS3PermittedByIAMRoles() {
        if (submissionAWSS3PermittedByIAMRoles != null) {
            return submissionAWSS3PermittedByIAMRoles;
        }
        if (globalAWSS3PermittedByIAMRoles != null) {
            return globalAWSS3PermittedByIAMRoles;
        }

        throw new RuntimeException("Missing aws.s3.permittedByIAMRoles or submission.aws.s3.permittedByIAMRoles in " + confLocation);
    }
    
    private void build() {
        urielBaseUrl = requireStringValue("uriel.baseUrl");
        urielBaseDataDir = requireDirectoryValue("uriel.baseDataDir");
        urielStressTestSecret = requireStringValue("uriel.stressTestSecret");

        jophielBaseUrl = requireStringValue("jophiel.baseUrl");
        jophielClientJid = requireStringValue("jophiel.clientJid");
        jophielClientSecret = requireStringValue("jophiel.clientSecret");

        sandalphonBaseUrl = requireStringValue("sandalphon.baseUrl");

        sealtielBaseUrl = requireStringValue("sealtiel.baseUrl");
        sealtielClientJid = requireStringValue("sealtiel.clientJid");
        sealtielClientSecret = requireStringValue("sealtiel.clientSecret");
        sealtielGabrielClientJid = requireStringValue("sealtiel.gabrielClientJid");

        globalAWSAccessKey = getStringValue("aws.accessKey");
        globalAWSSecretKey = getStringValue("aws.secretKey");
        globalAWSS3Region = Region.fromValue(getStringValue("aws.s3.bucket.regionId"));
        globalAWSS3PermittedByIAMRoles = getBooleanValue("aws.s3.permittedByIAMRoles");

        teamAvatarAWSS3Use = requireBooleanValue("teamAvatar.aws.s3.use");
        teamAvatarAWSAccessKey = getStringValue("teamAvatar.aws.accessKey");
        teamAvatarAWSSecretKey = getStringValue("teamAvatar.aws.secretKey");
        teamAvatarAWSS3BucketName = getStringValue("teamAvatar.aws.s3.bucket.name");
        teamAvatarAWSS3BucketRegion = Region.fromValue(getStringValue("teamAvatar.aws.s3.bucket.regionId"));
        teamAvatarAWSS3PermittedByIAMRoles = getBooleanValue("teamAvatar.aws.s3.permittedByIAMRoles");

        if (teamAvatarAWSS3Use) {
            teamAvatarAWSCloudFrontUrl = requireStringValue("teamAvatar.aws.cloudFront.baseUrl");
        } else {
            try {
                teamAvatarLocalDir = new File(urielBaseDataDir, "teamAvatar");
                FileUtils.forceMkdir(teamAvatarLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        submissionAWSS3Use = requireBooleanValue("submission.aws.s3.use");
        submissionAWSAccessKey = getStringValue("submission.aws.accessKey");
        submissionAWSSecretKey = getStringValue("submission.aws.secretKey");
        submissionAWSS3BucketName = getStringValue("submission.aws.s3.bucket.name");
        submissionAWSS3BucketRegion = Region.fromValue(getStringValue("submission.aws.s3.bucket.regionId"));
        submissionAWSS3PermittedByIAMRoles = getBooleanValue("submission.aws.s3.permittedByIAMRoles");

        if (!submissionAWSS3Use) {
            try {
                submissionLocalDir = new File(urielBaseDataDir, "submission");
                FileUtils.forceMkdir(submissionLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getStringValue(String key) {
        return conf.getString(key);
    }

    private String requireStringValue(String key) {
        String value = getStringValue(key);
        if (value == null) {
            throw new RuntimeException("Missing " + key + " property in " + confLocation);
        }
        return value;
    }

    private Boolean getBooleanValue(String key) {
        return conf.getBoolean(key);
    }

    private boolean requireBooleanValue(String key) {
        Boolean value = getBooleanValue(key);
        if (value == null) {
            throw new RuntimeException("Missing " + key + " property in " + confLocation);
        }
        return value;
    }

    private File requireDirectoryValue(String key) {
        String filename = getStringValue(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Directory " + key + " does not exist");
        }
        return dir;
    }
}
