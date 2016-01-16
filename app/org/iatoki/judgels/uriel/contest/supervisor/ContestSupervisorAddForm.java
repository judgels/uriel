package org.iatoki.judgels.uriel.contest.supervisor;

import play.data.validation.Constraints;

import java.util.Map;

public final class ContestSupervisorAddForm {

    @Constraints.Required
    public String username;

    public boolean isAllowedAll;

    public Map<String, String> allowedPermissions;
}
