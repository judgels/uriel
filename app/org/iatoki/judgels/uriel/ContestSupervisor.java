package org.iatoki.judgels.uriel;

public final class ContestSupervisor {

    private final long id;

    private final String contestJid;

    private final String userJid;

    private final boolean announcement;

    private final boolean problem;

    private final boolean submission;

    private final boolean clarification;

    private final boolean contestant;

    public ContestSupervisor(long id, String contestJid, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant) {
        this.id = id;
        this.contestJid = contestJid;
        this.userJid = userJid;
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
