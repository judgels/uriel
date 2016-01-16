package org.iatoki.judgels.uriel.contest.supervisor;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import java.util.Set;

public final class ContestPermission {

    private final Set<String> allowedPermissions;
    private final boolean isAllowedAll;

    public ContestPermission(Set<String> allowedPermissions, boolean isAllowedAll) {
        this.allowedPermissions = allowedPermissions;
        this.isAllowedAll = isAllowedAll;
    }

    public static ContestPermission defaultRestriction() {
        return new ContestPermission(ImmutableSet.of(), true);
    }

    public static ContestPermission fromJSONString(String json) {
        return new Gson().fromJson(json, ContestPermission.class);
    }

    public Set<String> getAllowedPermissions() {
        return allowedPermissions;
    }

    public boolean isAllowedAll() {
        return isAllowedAll;
    }

    public boolean isAllowed(ContestPermissions permission) {
        return isAllowedAll || allowedPermissions.contains(permission.name());
    }

    public String toJSONString() {
        return new Gson().toJson(this);
    }
}
