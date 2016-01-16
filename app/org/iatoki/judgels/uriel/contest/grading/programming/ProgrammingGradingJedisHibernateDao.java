package org.iatoki.judgels.uriel.contest.grading.programming;

import org.iatoki.judgels.sandalphon.models.daos.jedishibernate.AbstractProgrammingGradingJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ProgrammingGradingJedisHibernateDao extends AbstractProgrammingGradingJedisHibernateDao<ProgrammingGradingModel> implements ProgrammingGradingDao {

    @Inject
    public ProgrammingGradingJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProgrammingGradingModel.class);
    }

    @Override
    public ProgrammingGradingModel createGradingModel() {
        return new ProgrammingGradingModel();
    }
}
