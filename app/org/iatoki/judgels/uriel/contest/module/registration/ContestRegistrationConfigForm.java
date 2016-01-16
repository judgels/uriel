package org.iatoki.judgels.uriel.contest.module.registration;

import play.data.validation.Constraints;

public final class ContestRegistrationConfigForm {

    @Constraints.Required
    public String registerStartTime;

    public boolean manualApproval;

    @Constraints.Required
    public long registerDuration;

    @Constraints.Required
    public long maxRegistrants;
}
