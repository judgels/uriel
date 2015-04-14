package org.iatoki.judgels.uriel;

public class ContestTypeConfigStandard implements ContestTypeConfig {

    public static ContestTypeConfigStandard defaultConfig(Contest contest) {
        return new ContestTypeConfigStandard(contest.getEndTime().getTime(), true);
    }

    public ContestTypeConfigStandard(long scoreboardFreezeTime, boolean isOfficialScoreboardAllowed) {
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
