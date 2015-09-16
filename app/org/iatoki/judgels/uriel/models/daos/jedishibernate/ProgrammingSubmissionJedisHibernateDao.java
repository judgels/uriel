package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.sandalphon.models.daos.jedishibernate.AbstractProgrammingSubmissionJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.uriel.models.entities.ProgrammingSubmissionModel;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("programmingSubmissionDao")
public final class ProgrammingSubmissionJedisHibernateDao extends AbstractProgrammingSubmissionJedisHibernateDao<ProgrammingSubmissionModel> implements ProgrammingSubmissionDao {

    @Inject
    public ProgrammingSubmissionJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ProgrammingSubmissionModel.class);
    }

    @Override
    public ProgrammingSubmissionModel createSubmissionModel() {
        return new ProgrammingSubmissionModel();
    }
}
