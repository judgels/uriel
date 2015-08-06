package org.iatoki.judgels.uriel.modules.registration;

import play.data.validation.Constraints;

public final class ContestRegistrationConfigForm {

    @Constraints.Required
    public String registerStartTime;

    @Constraints.Required
    public long registerDuration;

    @Constraints.Required
    public long maxRegistrants;
}
