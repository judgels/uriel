package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestSupervisorCreateForm {

    @Constraints.Required

    public String userJid;

    public boolean announcement;

    public boolean problem;

    public boolean submission;

    public boolean clarification;

    public boolean contestant;

}
