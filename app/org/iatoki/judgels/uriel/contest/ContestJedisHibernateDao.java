package org.iatoki.judgels.uriel.contest;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ContestJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ContestModel> implements ContestDao {

    @Inject
    public ContestJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestModel.class);
    }

    @Override
    protected List<SingularAttribute<ContestModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ContestModel_.name, ContestModel_.description, ContestModel_.style);
    }
}
