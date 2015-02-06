package org.iatoki.judgels.uriel;

public final class ContestPermission {

    private long id;

    private String contestJid;

    private String userJid;

    private String username;

    private String alias;

    private boolean announcement;

    private boolean problem;

    private boolean submission;

    private boolean clarification;

    private boolean contestant;

    public ContestPermission(long id, String contestJid, String userJid, String username, String alias, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
        this.username = username;
        this.alias = alias;
        this.announcement = announcement;
        this.problem = problem;
        this.submission = submission;
        this.clarification = clarification;
        this.contestant = contestant;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getUsername() {
        return username;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isAnnouncement() {
        return announcement;
    }

    public boolean isProblem() {
        return problem;
    }

    public boolean isSubmission() {
        return submission;
    }

    public boolean isClarification() {
        return clarification;
    }

    public boolean isContestant() {
        return contestant;
    }
}
