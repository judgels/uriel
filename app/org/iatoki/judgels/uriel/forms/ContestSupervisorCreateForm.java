package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

import java.util.Map;

public final class ContestSupervisorCreateForm {

    @Constraints.Required
    public String username;

    public boolean isAllowedAll;

    public Map<String, String> allowedPermissions;
}
