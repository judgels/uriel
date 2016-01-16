package org.iatoki.judgels.uriel.contest.module.virtual;

import play.data.validation.Constraints;

public final class ContestVirtualConfigForm {

    @Constraints.Required
    public long virtualDuration;
}
