package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestModel_;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("contestDao")
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
