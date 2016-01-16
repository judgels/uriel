package org.iatoki.judgels.uriel.contest.style;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
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
