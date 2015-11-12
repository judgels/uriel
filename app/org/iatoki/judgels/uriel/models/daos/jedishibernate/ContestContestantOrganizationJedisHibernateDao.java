package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantOrganizationDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantOrganizationModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantOrganizationModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("ContestContestantOrganizationDao")
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
