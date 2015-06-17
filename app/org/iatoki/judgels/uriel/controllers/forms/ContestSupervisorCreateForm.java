package org.iatoki.judgels.uriel.controllers.forms;

import play.data.validation.Constraints;

public final class ContestSupervisorCreateForm {

    @Constraints.Required
    public String username;

    public boolean announcement;

    public boolean problem;

    public boolean submission;

    public boolean clarification;

    public boolean contestant;

}
