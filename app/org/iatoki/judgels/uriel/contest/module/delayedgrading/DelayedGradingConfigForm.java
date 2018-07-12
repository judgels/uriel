package org.iatoki.judgels.uriel.contest.module.delayedgrading;

import play.data.validation.Constraints;

public final class DelayedGradingConfigForm {
    @Constraints.Required
    public long delayDuration;
}
