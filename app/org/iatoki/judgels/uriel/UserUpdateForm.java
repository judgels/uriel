package org.iatoki.judgels.uriel;

import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

public final class UserUpdateForm {

    public UserUpdateForm() {

    }

    public UserUpdateForm(User userRole) {
        this.roles = StringUtils.join(userRole.getRoles(), ",");
    }

    @Constraints.Required
    public String roles;
}
