package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestContestantHibernateDao extends AbstractHibernateDao<Long, ContestContestantModel> implements ContestContestantDao {

    @Override
    public boolean isExistByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.userJid), contestantJid), cb.equal(root.get(ContestContestantModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestContestantModel findByContestantJid(String contestId, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(ContestContestantModel.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        query
            .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestId), cb.equal(root.get(ContestContestantModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countByFilter(String contestJid, String filterString, List<String> userJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestContestantModel_.userJid), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestContestantModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestContestantModel_.userJid).in(userJids));
        }

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), condition));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestContestantModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantModel> query = cb.createQuery(ContestContestantModel.class);
        Root<ContestContestantModel> root = query.from(ContestContestantModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestContestantModel_.userJid), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestContestantModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestContestantModel_.userJid).in(userJids));
        }

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .where(cb.and(cb.equal(root.get(ContestContestantModel_.contestJid), contestJid), condition))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }
}
