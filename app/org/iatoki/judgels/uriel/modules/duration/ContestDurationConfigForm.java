package org.iatoki.judgels.uriel.modules.duration;

import play.data.validation.Constraints;

public final class ContestDurationConfigForm {

    @Constraints.Required
    public String beginTime;

    @Constraints.Required
    public long contestDuration;
}
