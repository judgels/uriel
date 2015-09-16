package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestStyleDao;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("contestModuleDao")
public final class ContestStyleJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestStyleModel> implements ContestStyleDao {

    @Inject
    public ContestStyleJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestStyleModel.class);
    }

    @Override
    public ContestStyleModel findInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestStyleModel> query = cb.createQuery(ContestStyleModel.class);
        Root<ContestStyleModel> root = query.from(ContestStyleModel.class);

        query.where(cb.equal(root.get(ContestStyleModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
