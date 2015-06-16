package org.iatoki.judgels.uriel.controllers.forms;

import play.data.validation.Constraints;

public final class ContestManagerCreateForm {

    @Constraints.Required
    public String username;
}
