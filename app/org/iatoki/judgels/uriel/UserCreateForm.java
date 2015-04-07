package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class UserCreateForm {

    public UserCreateForm() {

    }

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String roles;
}
