package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.LanguageRestriction;

public class ContestStyleConfigIOI implements ContestStyleConfig {
    private final LanguageRestriction languageRestriction;

    public static ContestStyleConfigIOI defaultConfig(Contest contest) {
        return new ContestStyleConfigIOI(LanguageRestriction.defaultRestriction());
    }

    public ContestStyleConfigIOI(LanguageRestriction languageRestriction) {
        this.languageRestriction = languageRestriction;
    }

    public LanguageRestriction getLanguageRestriction() {
        if (languageRestriction == null) {
            return LanguageRestriction.defaultRestriction();
        } else {
            return languageRestriction;
        }
    }
}
