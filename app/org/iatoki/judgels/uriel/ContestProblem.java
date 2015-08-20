package org.iatoki.judgels.uriel;

public final class ContestProblem {

    private final long id;
    private final String contestJid;
    private final String problemJid;
    private final String problemSecret;
    private final String alias;
    private final long submissionsLimit;
    private final ContestProblemStatus status;
    private long totalSubmissions;

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

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getSubmissionsLeft() {
        return submissionsLimit - totalSubmissions;
    }
}
