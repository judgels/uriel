package org.iatoki.judgels.uriel.contest.contestant.organization;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public class ContestContestantOrganizationJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestContestantOrganizationModel> implements ContestContestantOrganizationDao {

    @Inject
    public ContestContestantOrganizationJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestContestantOrganizationModel.class);
    }

    @Override
    public ContestContestantOrganizationModel findInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantOrganizationModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantOrganizationModel> root = query.from(getModelClass());

        query.where(cb.and(cb.equal(root.get(ContestContestantOrganizationModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantOrganizationModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
