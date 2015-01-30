package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, HH:mm");
        return sdf.format(date);
    }

    public static Date convertStringToDate(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, HH:mm");
            return sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getDefaultRole() {
        return ImmutableList.of("user");
    }

    public static void saveRoleInSession(List<String> roles) {
        Http.Context.current().session().put("role", StringUtils.join(roles, ","));

    }

    public static boolean hasRole(String role) {
        return Arrays.asList(Http.Context.current().session().get("role").split(",")).contains(role);
    }

}
