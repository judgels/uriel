package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestContestantHibernateDao extends AbstractHibernateDao<Long, ContestContestantModel> implements ContestContestantDao {

    public ContestContestantHibernateDao() {
        super(ContestContestantModel.class);
    }

    @Override
    public boolean existsByContestJidAndContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.status), ContestContestantStatus.APPROVED.name())));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestContestantModel findByContestJidAndContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countContestContestantByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean isContestStarted(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.contestStartTime), 0)));

        return (JPA.em().createQuery(query).getSingleResult() == 0);
    }

    @Override
    public boolean isThereNewContestant(String contestJid, long lastTime) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.gt(root.get(ContestContestantModel_.timeUpdate), lastTime)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<String> findContestJidsByContestantJid(String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .select(root.get(ContestContestantModel_.contestJid))
            .where(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestContestantModel> findAllByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .where(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
