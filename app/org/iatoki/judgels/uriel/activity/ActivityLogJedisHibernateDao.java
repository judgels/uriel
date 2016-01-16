package org.iatoki.judgels.uriel.activity;

import org.iatoki.judgels.jophiel.models.daos.jedishibernate.AbstractActivityLogJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
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
