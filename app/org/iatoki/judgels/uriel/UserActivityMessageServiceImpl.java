package org.iatoki.judgels.uriel;

import org.iatoki.judgels.jophiel.services.impls.DefaultUserActivityMessageServiceImpl;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;

import java.util.List;

public final class UserActivityMessageServiceImpl implements UserActivityMessageService {

    private static final UserActivityMessageServiceImpl INSTANCE = new UserActivityMessageServiceImpl();

    @Override
    public void addUserActivityMessage(UserActivityMessage userActivityMessage) throws InterruptedException {
        DefaultUserActivityMessageServiceImpl.getInstance().addUserActivityMessage(userActivityMessage);
    }

    @Override
    public void addUserActivityMessages(List<UserActivityMessage> userActivityMessages) throws InterruptedException {
        DefaultUserActivityMessageServiceImpl.getInstance().addUserActivityMessages(userActivityMessages);
    }

    @Override
    public List<UserActivityMessage> getUserActivityMessages() throws InterruptedException {
        return DefaultUserActivityMessageServiceImpl.getInstance().getUserActivityMessages();
    }

    public static UserActivityMessageServiceImpl getInstance() {
        return INSTANCE;
    }
}
