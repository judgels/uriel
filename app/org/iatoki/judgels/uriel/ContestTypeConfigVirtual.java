package org.iatoki.judgels.uriel;

import java.util.concurrent.TimeUnit;

public class ContestTypeConfigVirtual implements ContestTypeConfig {

    public static ContestTypeConfigVirtual defaultConfig(Contest contest) {
        return new ContestTypeConfigVirtual(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
    }

    public ContestTypeConfigVirtual(long contestDuration) {
        this.contestDuration = contestDuration;
    }

    private long contestDuration;

    public long getContestDuration() {
        return contestDuration;
    }
}
