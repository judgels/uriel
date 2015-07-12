package org.iatoki.judgels.uriel.forms;

import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

import java.util.Arrays;
import java.util.List;

public final class UserCreateForm {
    public UserCreateForm() {

    }

    public UserCreateForm(List<String> roles) {
        this.roles = StringUtils.join(roles, ",");
    }

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String roles;

    public List<String> getRolesAsList() {
        return Arrays.asList(this.roles.split(","));
    }
}
