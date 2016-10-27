package org.iatoki.judgels.uriel.contest.contestant;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ContestContestantHibernateDao extends AbstractHibernateDao<Long, ContestContestantModel> implements ContestContestantDao {

    public ContestContestantHibernateDao() {
        super(ContestContestantModel.class);
    }

    @Override
    public boolean existsInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsInContestByContestantJid(String contestJid, String contestantJid, String status) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.status), status)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestContestantModel findInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean hasContestantStarted(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantModel_.contestStartTime), 0)));

        return (JPA.em().createQuery(query).getSingleResult() == 0);
    }

    @Override
    public List<String> getContestJidsByJid(String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
            .select(root.get(ContestContestantModel_.contestJid))
            .where(cb.and(
                    cb.equal(root.get(ContestContestantModel_.userJid), contestantJid),
                    cb.equal(root.get(ContestContestantModel_.status), "APPROVED")));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestContestantModel> getAllInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantModel> root = query.from(getModelClass());

        query
                .where(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
