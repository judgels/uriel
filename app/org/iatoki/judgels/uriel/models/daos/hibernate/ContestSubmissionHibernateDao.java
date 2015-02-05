package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.ContestSubmission;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestSubmissionHibernateDao extends AbstractJudgelsHibernateDao<ContestSubmissionModel> implements ContestSubmissionDao {

    @Override
    public long countByFilters(String contestJid, String problemJid, String authorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestSubmissionModel> root = query.from(ContestSubmissionModel.class);

        List<Predicate> predicates = new ArrayList<>();

        if (contestJid != null) {
            predicates.add(cb.equal(root.get("contestJid"), contestJid));
        }
        if (problemJid != null) {
            predicates.add(cb.equal(root.get("problemJid"), problemJid));
        }
        if (authorJid != null) {
            predicates.add(cb.equal(root.get("authorJid"), authorJid));
        }

        Predicate condition = cb.and(predicates.toArray(new Predicate[predicates.size()]));

        query
                .select(cb.count(root))
                .where(condition);

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestSubmissionModel> findByFilters(String contestJid, String problemJid, String authorJid) {

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestSubmissionModel> query = cb.createQuery(ContestSubmissionModel.class);
        Root<ContestSubmissionModel> root = query.from(ContestSubmissionModel.class);

        List<Predicate> predicates = new ArrayList<>();

        if (contestJid != null) {
            predicates.add(cb.equal(root.get("contestJid"), contestJid));
        }
        if (problemJid != null) {
            predicates.add(cb.equal(root.get("problemJid"), problemJid));
        }
        if (authorJid != null) {
            predicates.add(cb.equal(root.get("authorJid"), authorJid));
        }

        Predicate condition = cb.and(predicates.toArray(new Predicate[predicates.size()]));

        query.where(condition);

        return JPA.em().createQuery(query).getResultList();
    }
}
