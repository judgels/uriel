package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestClarificationHibernateDao extends AbstractHibernateDao<Long, ContestClarificationModel> implements ContestClarificationDao {

    @Override
    public List<ContestClarificationModel> findByContestJidAndAskerJid(String contestJid, String askerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestClarificationModel> query = cb.createQuery(ContestClarificationModel.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query.where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), cb.equal(root.get(AbstractJudgelsModel_.userCreate), askerJid)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public long countByFilter(String contestJid, String filterString, List<String> userJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestClarificationModel_.question), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestClarificationModel_.answer), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestClarificationModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(AbstractJudgelsModel_.userCreate).in(userJids), root.get(AbstractJudgelsModel_.userUpdate).in(userJids));
        }

        query
            .select(cb.count(root))
            .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), condition));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestClarificationModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestClarificationModel> query = cb.createQuery(ContestClarificationModel.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestClarificationModel_.question), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestClarificationModel_.answer), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestClarificationModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));
        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(AbstractJudgelsModel_.userCreate).in(userJids), root.get(AbstractJudgelsModel_.userUpdate).in(userJids));
        }

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), condition))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }
}
