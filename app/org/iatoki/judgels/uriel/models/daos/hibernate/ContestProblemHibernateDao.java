package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestProblemHibernateDao extends AbstractHibernateDao<Long, ContestProblemModel> implements ContestProblemDao {

    @Override
    public List<ContestProblemModel> findByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .where(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestProblemModel findByProblemJid(String contestJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
            .where(cb.equal(root.get(ContestProblemModel_.problemJid), problemJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean isExistByProblemJid(String contestJid, String problemJid) {
        return false;
    }

    @Override
    public List<ContestProblemModel> findOpenedByContestJidOrderedByAlias(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        Order orderBy = cb.asc(root.get(ContestProblemModel_.alias));

        query
                .where(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public long countByFilter(String contestJid, String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestProblemModel_.alias), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestProblemModel_.name), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestProblemModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), condition));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestProblemModel> findByContestJidFilterAndSort(String contestJid, String filterString, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestProblemModel_.alias), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestProblemModel_.name), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestProblemModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), condition))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }

}
