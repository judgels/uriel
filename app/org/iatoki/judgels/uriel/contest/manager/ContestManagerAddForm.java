package org.iatoki.judgels.uriel.contest.manager;

import play.data.validation.Constraints;

public final class ContestManagerAddForm {

    @Constraints.Required
    public String username;
}
