package org.iatoki.judgels.uriel.activity;

import org.iatoki.judgels.jophiel.models.daos.BaseActivityLogDao;
import org.iatoki.judgels.jophiel.services.impls.AbstractBaseActivityLogServiceImpl;

public final class ActivityLogServiceImpl extends AbstractBaseActivityLogServiceImpl<ActivityLogModel> implements ActivityLogService {

    private static ActivityLogServiceImpl INSTANCE;

    private ActivityLogServiceImpl(BaseActivityLogDao<ActivityLogModel> activityLogDao) {
        super(activityLogDao);
    }

    public static synchronized void buildInstance(BaseActivityLogDao<ActivityLogModel> activityLogDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ActivityLogService instance has already been built");
        }
        INSTANCE = new ActivityLogServiceImpl(activityLogDao);
    }

    public static ActivityLogServiceImpl getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ActivityLogService instance has not been built");
        }
        return INSTANCE;
    }
}
