package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public class StandardContestTypeConfigForm {

    public StandardContestTypeConfigForm() {
    }

    public StandardContestTypeConfigForm(String scoreboardFreezeTime, boolean isOfficialScoreboardAllowed) {
        this.scoreboardFreezeTime = scoreboardFreezeTime;
        this.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
    }

    @Constraints.Required
    public String scoreboardFreezeTime;

    @Constraints.Required
    public boolean isOfficialScoreboardAllowed;

}
