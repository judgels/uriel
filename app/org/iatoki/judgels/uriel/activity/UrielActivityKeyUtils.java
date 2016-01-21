package org.iatoki.judgels.uriel.activity;

import org.iatoki.judgels.jophiel.activity.ActivityKey;
import org.iatoki.judgels.jophiel.activity.BasicActivityKeyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class UrielActivityKeyUtils {

    private UrielActivityKeyUtils() {
        // prevent instantiation
    }

    public static ActivityKey fromJson(String keyAction, String json) {
        Method fromJson;
        Method getKeyAction;
        try {
            getKeyAction = ActivityKey.class.getMethod("getKeyAction");
            fromJson = ActivityKey.class.getMethod("fromJson", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Field field : UrielActivityKeys.class.getFields()) {
            try {
                Object activityKey = field.get(null);
                String key = (String) getKeyAction.invoke(field.get(null));
                if (keyAction.equals(key)) {
                    return (ActivityKey) fromJson.invoke(activityKey, json);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return BasicActivityKeyUtils.fromJson(keyAction, json);
    }
}
