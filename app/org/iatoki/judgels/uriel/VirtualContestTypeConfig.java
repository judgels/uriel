package org.iatoki.judgels.uriel;

import java.util.concurrent.TimeUnit;

public class VirtualContestTypeConfig implements ContestTypeConfig {

    public static VirtualContestTypeConfig defaultConfig(Contest contest) {
        return new VirtualContestTypeConfig(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS), VirtualContestTypeConfigStartTrigger.CONTESTANT);
    }

    public VirtualContestTypeConfig(long contestDuration, VirtualContestTypeConfigStartTrigger startTrigger) {
        this.contestDuration = contestDuration;
        this.startTrigger = startTrigger;
    }

    private final long contestDuration;

    private final VirtualContestTypeConfigStartTrigger startTrigger;

    public long getContestDuration() {
        return contestDuration;
    }

    public VirtualContestTypeConfigStartTrigger getStartTrigger() {
        return startTrigger;
    }
}
