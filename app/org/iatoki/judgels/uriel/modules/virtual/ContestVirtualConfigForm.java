package org.iatoki.judgels.uriel.modules.virtual;

import play.data.validation.Constraints;

public final class ContestVirtualConfigForm {

    @Constraints.Required
    public long virtualDuration;
}
