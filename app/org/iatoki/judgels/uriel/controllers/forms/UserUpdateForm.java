package org.iatoki.judgels.uriel.controllers.forms;

import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

import java.util.Arrays;
import java.util.List;

public final class UserUpdateForm {
    public UserUpdateForm() {

    }

    public UserUpdateForm(List<String> roles) {
        this.roles = StringUtils.join(roles, ",");
    }

    @Constraints.Required
    public String roles;

    public List<String> getRolesAsList() {
        return Arrays.asList(this.roles.split(","));
    }
}
