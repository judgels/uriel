package org.iatoki.judgels.uriel.forms;

import java.util.Map;

public class IOIContestStyleConfigForm {

    public IOIContestStyleConfigForm() {
    }

    public IOIContestStyleConfigForm(boolean isAllowedAll, Map<String, String> allowedLanguageNames) {
        this.isAllowedAll = isAllowedAll;
        this.allowedLanguageNames = allowedLanguageNames;
    }

    public boolean isAllowedAll;
    public Map<String, String> allowedLanguageNames;
}
