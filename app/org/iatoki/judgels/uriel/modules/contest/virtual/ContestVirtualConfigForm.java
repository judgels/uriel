package org.iatoki.judgels.uriel.modules.contest.virtual;

import play.data.validation.Constraints;

public final class ContestVirtualConfigForm {

    @Constraints.Required
    public long virtualDuration;
}
