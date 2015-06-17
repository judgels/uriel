package org.iatoki.judgels.uriel.controllers.forms;

import java.util.Map;

public class ContestStyleConfigIOIForm {

    public ContestStyleConfigIOIForm() {
    }

    public ContestStyleConfigIOIForm(boolean isAllowedAll, Map<String, String> allowedLanguageNames) {
        this.isAllowedAll = isAllowedAll;
        this.allowedLanguageNames = allowedLanguageNames;
    }

    public boolean isAllowedAll;
    public Map<String, String> allowedLanguageNames;
}
