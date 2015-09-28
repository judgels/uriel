package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.jophiel.models.daos.jedishibernate.AbstractActivityLogJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ActivityLogDao;
import org.iatoki.judgels.uriel.models.entities.ActivityLogModel;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("activityLogDao")
public final class ActivityLogJedisHibernateDao extends AbstractActivityLogJedisHibernateDao<ActivityLogModel> implements ActivityLogDao {

    @Inject
    public ActivityLogJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ActivityLogModel.class);
    }

    @Override
    public ActivityLogModel createActivityLogModel() {
        return new ActivityLogModel();
    }
}
