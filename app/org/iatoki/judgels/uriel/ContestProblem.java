package org.iatoki.judgels.uriel;

public final class ContestProblem {

    private long id;

    private String contestJid;

    private String problemJid;

    private String problemSecret;

    private String alias;

    private long submissionsLimit;

    private ContestProblemStatus status;

    public ContestProblem(long id, String contestJid, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status) {
        this.id = id;
        this.contestJid = contestJid;
        this.problemJid = problemJid;
        this.problemSecret = problemSecret;
        this.alias = alias;
        this.submissionsLimit = submissionsLimit;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getProblemJid() {
        return problemJid;
    }

    public String getProblemSecret() {
        return problemSecret;
    }

    public String getAlias() {
        return alias;
    }

    public long getSubmissionsLimit() {
        return submissionsLimit;
    }

    public ContestProblemStatus getStatus() {
        return status;
    }
}
