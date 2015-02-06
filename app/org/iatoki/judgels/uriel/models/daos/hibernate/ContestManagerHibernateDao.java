package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestManagerHibernateDao extends AbstractHibernateDao<Long, ContestManagerModel> implements ContestManagerDao {

    @Override
    public boolean isExistByManagerJid(String contestJid, String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.userJid), managerJid), cb.equal(root.get(ContestManagerModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestManagerModel findByManagerJid(String contestJid, String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestManagerModel> query = cb.createQuery(ContestManagerModel.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.userJid), managerJid), cb.equal(root.get(ContestManagerModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countByFilter(String contestJid, String filterString, List<String> userJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestManagerModel_.userJid), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestManagerModel_.userJid).in(userJids));
        }

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.contestJid), contestJid), condition));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestManagerModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestManagerModel> query = cb.createQuery(ContestManagerModel.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestManagerModel_.userJid), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestManagerModel_.userJid).in(userJids));
        }

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.contestJid), contestJid), condition))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }
}
