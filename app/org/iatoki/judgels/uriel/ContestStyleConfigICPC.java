package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestriction;

import java.util.concurrent.TimeUnit;

public class ContestStyleConfigICPC implements ContestStyleConfig {
    private final long timePenalty;

    private final LanguageRestriction languageRestriction;

    public static ContestStyleConfigICPC defaultConfig(Contest contest) {
        return new ContestStyleConfigICPC(TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES), LanguageRestriction.defaultRestriction());
    }

    public ContestStyleConfigICPC(long timePenalty, LanguageRestriction languageRestriction) {
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
