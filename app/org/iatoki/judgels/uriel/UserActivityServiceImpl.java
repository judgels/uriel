package org.iatoki.judgels.uriel;

import org.iatoki.judgels.jophiel.commons.DefaultUserActivityServiceImpl;
import org.iatoki.judgels.jophiel.commons.UserActivity;
import org.iatoki.judgels.jophiel.commons.UserActivityService;

import java.util.List;

public final class UserActivityServiceImpl implements UserActivityService {

    private static final UserActivityServiceImpl INSTANCE = new UserActivityServiceImpl();

    @Override
    public void addUserActivity(UserActivity userActivity) throws InterruptedException {
        DefaultUserActivityServiceImpl.getInstance().addUserActivity(userActivity);
    }

    @Override
    public void addUserActivities(List<UserActivity> userActivities) throws InterruptedException {
        DefaultUserActivityServiceImpl.getInstance().addUserActivities(userActivities);
    }

    @Override
    public List<UserActivity> getUserActivities() throws InterruptedException {
        return DefaultUserActivityServiceImpl.getInstance().getUserActivities();
    }

    public static UserActivityServiceImpl getInstance() {
        return INSTANCE;
    }
}
