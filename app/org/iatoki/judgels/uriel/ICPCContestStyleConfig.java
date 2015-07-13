package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.LanguageRestriction;

import java.util.concurrent.TimeUnit;

public class ICPCContestStyleConfig implements ContestStyleConfig {
    private final long timePenalty;

    private final LanguageRestriction languageRestriction;

    public static ICPCContestStyleConfig defaultConfig(Contest contest) {
        return new ICPCContestStyleConfig(TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES), LanguageRestriction.defaultRestriction());
    }

    public ICPCContestStyleConfig(long timePenalty, LanguageRestriction languageRestriction) {
        this.timePenalty = timePenalty;
        this.languageRestriction = languageRestriction;
    }

    public long getTimePenalty() {
        return timePenalty;
    }

    public LanguageRestriction getLanguageRestriction() {
        if (languageRestriction == null) {
            return LanguageRestriction.defaultRestriction();
        } else {
            return languageRestriction;
        }
    }
}
