package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.user.User;
import play.mvc.Http;

import java.util.Arrays;
import java.util.List;

public final class UrielUtils {

    private UrielUtils() {
        // prevent instantiation
    }

    public static boolean isGuest() {
        return IdentityUtils.getUserJid().startsWith("guest");
    }

    public static List<String> getDefaultRoles() {
        return ImmutableList.of("user");
    }

    public static String getRolesFromSession() {
        return getFromSession("role");
    }

    public static void saveRolesInSession(List<String> roles) {
        putInSession("role", StringUtils.join(roles, ","));
    }

    public static boolean hasRole(String role) {
        return Http.Context.current().session().containsKey("role") && Arrays.asList(getFromSession("role").split(",")).contains(role);
    }

    public static void backupSession() {
        putInSession("realUserJid", getFromSession("userJid"));
        putInSession("realName", getFromSession("name"));
        putInSession("realUsername", getFromSession("username"));
        putInSession("realRole", getFromSession("role"));
        putInSession("realAvatar", getFromSession("avatar"));
    }

    public static void setUserSession(JophielUser publicUser, User user) {
        putInSession("userJid", publicUser.getJid());
        putInSession("name", publicUser.getName());
        putInSession("username", publicUser.getUsername());
        saveRolesInSession(user.getRoles());
        putInSession("avatar", publicUser.getProfilePictureUrl());
    }

    public static void restoreSession() {
        putInSession("userJid", getFromSession("realUserJid"));
        Http.Context.current().session().remove("realUserJid");
        putInSession("name", getFromSession("realName"));
        Http.Context.current().session().remove("realName");
        putInSession("username", getFromSession("realUsername"));
        Http.Context.current().session().remove("realUsername");
        putInSession("role", getFromSession("realRole"));
        Http.Context.current().session().remove("realRole");
        putInSession("avatar", getFromSession("realAvatar"));
        Http.Context.current().session().remove("realAvatar");
    }

    public static boolean trullyHasRole(String role) {
        if (Http.Context.current().session().containsKey("realRole")) {
            return Arrays.asList(getFromSession("realRole").split(",")).contains(role);
        } else {
            return hasRole(role);
        }
    }

    public static String getRealUserJid() {
        if (JudgelsPlayUtils.hasViewPoint()) {
            return getFromSession("realUserJid");
        } else {
            return IdentityUtils.getUserJid();
        }
    }

    public static String getRealUsername() {
        if (JudgelsPlayUtils.hasViewPoint()) {
            return getFromSession("realUsername");
        } else {
            return IdentityUtils.getUsername();
        }
    }

    private static void putInSession(String key, String value) {
        Http.Context.current().session().put(key, value);
    }

    private static String getFromSession(String key) {
        return Http.Context.current().session().get(key);
    }
}
