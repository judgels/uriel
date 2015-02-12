package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class ContestContestantHibernateDao extends AbstractHibernateDao<Long, ContestContestantModel> implements ContestContestantDao {

    public ContestContestantHibernateDao() {
        super(ContestContestantModel.class);
    }

    @Override
    public boolean existsByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestContestantModel findByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(ContestContestantModel.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        query
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
