package org.iatoki.judgels.uriel.contest.scoreboard;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public final class ContestScoreboardHibernateDao extends AbstractHibernateDao<Long, ContestScoreboardModel> implements ContestScoreboardDao {

    public ContestScoreboardHibernateDao() {
        super(ContestScoreboardModel.class);
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
