package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.jophiel.models.daos.hibernate.AbstractActivityLogHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ActivityLogDao;
import org.iatoki.judgels.uriel.models.entities.ActivityLogModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("activityLogDao")
public final class ActivityLogHibernateDao extends AbstractActivityLogHibernateDao<ActivityLogModel> implements ActivityLogDao {

    public ActivityLogHibernateDao() {
        super(ActivityLogModel.class);
    }

    @Override
    public ActivityLogModel createActivityLogModel() {
        return new ActivityLogModel();
    }
}
