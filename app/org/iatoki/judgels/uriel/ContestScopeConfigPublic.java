package org.iatoki.judgels.uriel;

public class ContestScopeConfigPublic implements ContestScopeConfig {

    public static ContestScopeConfigPublic defaultConfig(Contest contest) {
        return new ContestScopeConfigPublic(contest.getStartTime().getTime(), contest.getEndTime().getTime(), 500);
    }

    public ContestScopeConfigPublic(long registerStartTime, long registerEndTime, long maxRegistrants) {
        this.registerStartTime = registerStartTime;
        this.registerEndTime = registerEndTime;
        this.maxRegistrants = maxRegistrants;
    }

    private long registerStartTime;

    private long registerEndTime;

    private long maxRegistrants;

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