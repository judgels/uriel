package org.iatoki.judgels.uriel;

public class PublicContestScopeConfig implements ContestScopeConfig {

    public static PublicContestScopeConfig defaultConfig(Contest contest) {
        return new PublicContestScopeConfig(contest.getStartTime().getTime(), contest.getEndTime().getTime(), 0);
    }

    public PublicContestScopeConfig(long registerStartTime, long registerEndTime, long maxRegistrants) {
        this.registerStartTime = registerStartTime;
        this.registerEndTime = registerEndTime;
        this.maxRegistrants = maxRegistrants;
    }

    private final long registerStartTime;

    private final long registerEndTime;

    private final long maxRegistrants;

    public long getRegisterStartTime() {
        return registerStartTime;
    }

    public long getRegisterEndTime() {
        return registerEndTime;
    }

    public long getMaxRegistrants() {
        return maxRegistrants;
    }

}
