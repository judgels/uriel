package org.iatoki.judgels.uriel.forms;

import java.util.Map;

public class ICPCContestStyleConfigForm {

    public ICPCContestStyleConfigForm() {
    }

    public ICPCContestStyleConfigForm(long timePenalty, boolean isAllowedAll, Map<String, String> allowedLanguageNames) {
        this.timePenalty = timePenalty;
        this.isAllowedAll = isAllowedAll;
        this.allowedLanguageNames = allowedLanguageNames;
    }

    public long timePenalty;
    public boolean isAllowedAll;
    public Map<String, String> allowedLanguageNames;
}
