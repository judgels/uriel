package org.iatoki.judgels.uriel;

import java.util.concurrent.TimeUnit;

public class ContestTypeConfigVirtual implements ContestTypeConfig {

    public static ContestTypeConfigVirtual defaultConfig(Contest contest) {
        return new ContestTypeConfigVirtual(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS), ContestTypeConfigVirtualStartTrigger.CONTESTANT);
    }

    public ContestTypeConfigVirtual(long contestDuration, ContestTypeConfigVirtualStartTrigger startTrigger) {
        this.contestDuration = contestDuration;
        this.startTrigger = startTrigger;
    }

    private long contestDuration;

    private ContestTypeConfigVirtualStartTrigger startTrigger;

    public long getContestDuration() {
        return contestDuration;
    }

    public ContestTypeConfigVirtualStartTrigger getStartTrigger() {
        return startTrigger;
    }
}
