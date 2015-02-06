package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestPermissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestPermissionModel;
import org.iatoki.judgels.uriel.models.domains.ContestPermissionModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestPermissionHibernateDao extends AbstractHibernateDao<Long, ContestPermissionModel> implements ContestPermissionDao {

    @Override
    public boolean isExistBySupervisorJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestPermissionModel> root = query.from(ContestPermissionModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestPermissionModel_.userJid), supervisorJid), cb.equal(root.get(ContestPermissionModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestPermissionModel findBySupervisorJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestPermissionModel> query = cb.createQuery(ContestPermissionModel.class);
        Root<ContestPermissionModel> root = query.from(ContestPermissionModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestPermissionModel_.userJid), supervisorJid), cb.equal(root.get(ContestPermissionModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countByFilter(String contestJid, String filterString, List<String> userJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestPermissionModel> root = query.from(ContestPermissionModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestPermissionModel_.userJid), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestPermissionModel_.userJid).in(userJids));
        }

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestPermissionModel_.contestJid), contestJid), condition));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestPermissionModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestPermissionModel> query = cb.createQuery(ContestPermissionModel.class);
        Root<ContestPermissionModel> root = query.from(ContestPermissionModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestPermissionModel_.userJid), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(ContestPermissionModel_.userJid).in(userJids));
        }

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .where(cb.and(cb.equal(root.get(ContestPermissionModel_.contestJid), contestJid), condition))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }
}
