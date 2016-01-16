package org.iatoki.judgels.uriel.contest.module.trigger;

import play.data.validation.Constraints;

public final class ContestTriggerConfigForm {

    @Constraints.Required
    public String contestTrigger;
}
