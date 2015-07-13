package org.iatoki.judgels.uriel.forms;

import java.util.Map;

public class ICPCContestStyleConfigForm {

    public ICPCContestStyleConfigForm() {
    }

    public ICPCContestStyleConfigForm(long wrongSubmissionPenalty, boolean isAllowedAll, Map<String, String> allowedLanguageNames) {
        this.wrongSubmissionPenalty = wrongSubmissionPenalty;
        this.isAllowedAll = isAllowedAll;
        this.allowedLanguageNames = allowedLanguageNames;
    }

    public long wrongSubmissionPenalty;
    public boolean isAllowedAll;
    public Map<String, String> allowedLanguageNames;
}
