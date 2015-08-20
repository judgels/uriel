package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.LanguageRestriction;

public class IOIContestStyleConfig implements ContestStyleConfig {

    private final LanguageRestriction languageRestriction;

    public IOIContestStyleConfig(LanguageRestriction languageRestriction) {
        this.languageRestriction = languageRestriction;
    }

    public static IOIContestStyleConfig defaultConfig(Contest contest) {
        return new IOIContestStyleConfig(LanguageRestriction.defaultRestriction());
    }

    public LanguageRestriction getLanguageRestriction() {
        if (languageRestriction == null) {
            return LanguageRestriction.defaultRestriction();
        } else {
            return languageRestriction;
        }
    }
}
