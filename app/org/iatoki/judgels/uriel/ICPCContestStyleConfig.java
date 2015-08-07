package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.LanguageRestriction;

public class ICPCContestStyleConfig implements ContestStyleConfig {

    private final long wrongSubmissionPenalty;

    private final LanguageRestriction languageRestriction;

    public ICPCContestStyleConfig(long wrongSubmissionPenalty, LanguageRestriction languageRestriction) {
        this.wrongSubmissionPenalty = wrongSubmissionPenalty;
        this.languageRestriction = languageRestriction;
    }

    public static ICPCContestStyleConfig defaultConfig(Contest contest) {
        return new ICPCContestStyleConfig(20, LanguageRestriction.defaultRestriction());
    }

    public long getWrongSubmissionPenalty() {
        return wrongSubmissionPenalty;
    }

    public LanguageRestriction getLanguageRestriction() {
        if (languageRestriction == null) {
            return LanguageRestriction.defaultRestriction();
        } else {
            return languageRestriction;
        }
    }
}
