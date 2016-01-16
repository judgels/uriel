package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import org.iatoki.judgels.sandalphon.LanguageRestriction;
import org.iatoki.judgels.uriel.contest.style.ContestStyleConfig;

public class IOIContestStyleConfig implements ContestStyleConfig {

    private final boolean usingLastAffectingPenalty;

    private final LanguageRestriction languageRestriction;

    public IOIContestStyleConfig(boolean usingLastAffectingPenalty, LanguageRestriction languageRestriction) {
        this.usingLastAffectingPenalty = usingLastAffectingPenalty;
        this.languageRestriction = languageRestriction;
    }

    public static IOIContestStyleConfig defaultConfig() {
        return new IOIContestStyleConfig(false, LanguageRestriction.defaultRestriction());
    }

    public boolean usingLastAffectingPenalty() {
        return usingLastAffectingPenalty;
    }

    public LanguageRestriction getLanguageRestriction() {
        if (languageRestriction == null) {
            return LanguageRestriction.defaultRestriction();
        } else {
            return languageRestriction;
        }
    }
}
