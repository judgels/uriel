package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import play.mvc.Http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UrielUtils {

    private UrielUtils() {
        // prevent instantiation
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
        return sdf.format(date);
    }

    public static Date convertStringToDate(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
            return sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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
        return Arrays.asList(getFromSession("role").split(",")).contains(role);
    }

    public static void backupSession() {
        putInSession("realUserJid", getFromSession("userJid"));
        putInSession("realName", getFromSession("name"));
        putInSession("realUsername", getFromSession("username"));
        putInSession("realRole", getFromSession("role"));
        putInSession("realAvatar", getFromSession("avatar"));
        putInSession("realEmail", getFromSession("email"));
    }

    public static void setUserSession(org.iatoki.judgels.jophiel.User user, User urielUser) {
        putInSession("userJid", user.getJid());
        putInSession("name", user.getName());
        putInSession("username", user.getUsername());
        saveRolesInSession(urielUser.getRoles());
        putInSession("avatar", user.getProfilePictureUrl().toString());
        putInSession("email", user.getEmail());
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
        putInSession("email", getFromSession("realEmail"));
        Http.Context.current().session().remove("realEmail");
    }

    public static boolean trullyHasRole(String role) {
        if (Http.Context.current().session().containsKey("realRole")) {
            return Arrays.asList(getFromSession("realRole").split(",")).contains(role);
        } else {
            return hasRole(role);
        }
    }

    public static String getRealUserJid() {
        if (JudgelsUtils.hasViewPoint()) {
            return getFromSession("realUserJid");
        } else {
            return IdentityUtils.getUserJid();
        }
    }

    private static void putInSession(String key, String value) {
        Http.Context.current().session().put(key, value);
    }

    private static String getFromSession(String key) {
        return Http.Context.current().session().get(key);
    }
}
