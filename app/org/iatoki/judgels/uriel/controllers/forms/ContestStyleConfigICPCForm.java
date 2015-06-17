package org.iatoki.judgels.uriel.controllers.forms;

import java.util.Map;

public class ContestStyleConfigICPCForm {

    public ContestStyleConfigICPCForm() {
    }

    public ContestStyleConfigICPCForm(long timePenalty, boolean isAllowedAll, Map<String, String> allowedLanguageNames) {
        this.timePenalty = timePenalty;
        this.isAllowedAll = isAllowedAll;
        this.allowedLanguageNames = allowedLanguageNames;
    }

    public long timePenalty;
    public boolean isAllowedAll;
    public Map<String, String> allowedLanguageNames;
}
