package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class UserRoleHibernateDao extends AbstractHibernateDao<Long, UserRoleModel> implements UserRoleDao {

    @Override
    public boolean isExistByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        query
            .select(cb.count(root))
            .where(cb.equal(root.get(UserRoleModel_.userJid), userJid));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public UserRoleModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserRoleModel> query = cb.createQuery(UserRoleModel.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        query.where(cb.equal(root.get(UserRoleModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public long countByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserRoleModel_.userJid), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.username), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.alias), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query
                .select(cb.count(root))
                .where(condition);

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<UserRoleModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserRoleModel> query = cb.createQuery(UserRoleModel.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserRoleModel_.userJid), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.username), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.alias), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
            .where(condition)
            .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }

    @Override
    public List<String> findUserJidByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserRoleModel_.userJid), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.username), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(UserRoleModel_.alias), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query
                .select(root.get(UserRoleModel_.userJid))
                .where(condition);

        return JPA.em().createQuery(query).getResultList();
    }
}
