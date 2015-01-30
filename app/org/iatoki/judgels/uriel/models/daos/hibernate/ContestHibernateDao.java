package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;

public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {

    @Override
    public long countByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestModel> root = query.from(ContestModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestModel_.name), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.type), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.scope), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.style), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query
                .select(cb.count(root))
                .where(condition);

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModel> query = cb.createQuery(ContestModel.class);
        Root<ContestModel> root = query.from(ContestModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get(AbstractJudgelsModel_.id));
        selection.add(root.get(ContestModel_.name));
        selection.add(root.get(ContestModel_.type));
        selection.add(root.get(ContestModel_.scope));
        selection.add(root.get(ContestModel_.style));
        selection.add(root.get(ContestModel_.startTime));
        selection.add(root.get(ContestModel_.endTime));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestModel_.name), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.type), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.scope), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestModel_.style), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .multiselect(selection)
                .where(condition)
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }

}
