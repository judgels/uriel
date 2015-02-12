package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestManagerCreateForm {

    @Constraints.Required
    public String userJid;
}
