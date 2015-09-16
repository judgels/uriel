package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("contestScoreboardDao")
public final class ContestScoreboardJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestScoreboardModel> implements ContestScoreboardDao {

    @Inject
    public ContestScoreboardJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestScoreboardModel.class);
    }

    @Override
    public ContestScoreboardModel findInContestByScoreboardType(String contestJid, String type) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestScoreboardModel> query = cb.createQuery(getModelClass());

        Root<ContestScoreboardModel> root = query.from(getModelClass());

        query.where(cb.and(cb.equal(root.get(ContestScoreboardModel_.contestJid), contestJid), cb.equal(root.get(ContestScoreboardModel_.type), type)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, String type) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ContestScoreboardModel> root = query.from(getModelClass());

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestScoreboardModel_.contestJid), contestJid), cb.equal(root.get(ContestScoreboardModel_.type), type)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
