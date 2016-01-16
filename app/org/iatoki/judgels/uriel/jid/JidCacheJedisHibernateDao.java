package org.iatoki.judgels.uriel.jid;

import org.iatoki.judgels.play.jid.AbstractJidCacheJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class JidCacheJedisHibernateDao extends AbstractJidCacheJedisHibernateDao<JidCacheModel> implements JidCacheDao {

    @Inject
    public JidCacheJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, JidCacheModel.class);
    }

    @Override
    public JidCacheModel createJidCacheModel() {
        return new JidCacheModel();
    }
}
