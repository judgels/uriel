package org.iatoki.judgels.uriel;

import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

public final class UserRoleUpdateForm {

    public UserRoleUpdateForm() {

    }

    public UserRoleUpdateForm(UserRole userRole) {
        this.roles = StringUtils.join(userRole.getRoles(), ",");
    }

    @Constraints.Required
    public String roles;
}
