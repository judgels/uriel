package org.iatoki.judgels.uriel;

public class ContestScopeConfigPublic implements ContestScopeConfig {

    public static ContestScopeConfigPublic defaultConfig(Contest contest) {
        return new ContestScopeConfigPublic(contest.getStartTime().getTime(), contest.getEndTime().getTime(), 0);
    }

    public ContestScopeConfigPublic(long registerStartTime, long registerEndTime, long maxRegistrants) {
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
