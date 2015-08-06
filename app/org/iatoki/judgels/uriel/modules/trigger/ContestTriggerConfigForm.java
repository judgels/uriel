package org.iatoki.judgels.uriel.modules.trigger;

import play.data.validation.Constraints;

public final class ContestTriggerConfigForm {

    @Constraints.Required
    public String contestTrigger;
}
