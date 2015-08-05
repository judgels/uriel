package org.iatoki.judgels.uriel;

public class StandardContestTypeConfig implements ContestTypeConfig {

    public static StandardContestTypeConfig defaultConfig(Contest contest) {
        return new StandardContestTypeConfig(contest.getEndTime().getTime(), true);
    }

    public StandardContestTypeConfig(long scoreboardFreezeTime, boolean isOfficialScoreboardAllowed) {
        this.scoreboardFreezeTime = scoreboardFreezeTime;
        this.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
    }

    private final long scoreboardFreezeTime;

    private final boolean isOfficialScoreboardAllowed;

    public long getScoreboardFreezeTime() {
        return scoreboardFreezeTime;
    }

    public boolean isOfficialScoreboardAllowed() {
        return isOfficialScoreboardAllowed;
    }
}
