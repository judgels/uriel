package org.iatoki.judgels.uriel;

import java.util.concurrent.TimeUnit;

public class ContestStyleConfigICPC implements ContestStyleConfig {

    public static ContestStyleConfigICPC defaultConfig(Contest contest) {
        return new ContestStyleConfigICPC(TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES));
    }

    public ContestStyleConfigICPC(long timePenalty) {
        this.timePenalty = timePenalty;
    }

    private long timePenalty;

    public long getTimePenalty() {
        return timePenalty;
    }
}
