package org.iatoki.judgels.uriel;

import org.apache.commons.lang3.StringUtils;

public final class UserRoleUpdateForm {

    public UserRoleUpdateForm(UserRole userRole) {
        this.roles = StringUtils.join(userRole.getRoles(), ",");
    }

    public String roles;
}
