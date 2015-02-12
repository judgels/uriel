package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestProblemHibernateDao extends AbstractHibernateDao<Long, ContestProblemModel> implements ContestProblemDao {

    public ContestProblemHibernateDao() {
        super(ContestProblemModel.class);
    }

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
    public boolean existsByProblemJid(String contestJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.problemJid), problemJid), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestProblemModel> findOpenedByContestJidOrderedByAlias(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        Order orderBy = cb.asc(root.get(ContestProblemModel_.alias));

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.equal(root.get(ContestProblemModel_.status), ContestProblemStatus.OPEN.name())))
                        .orderBy(orderBy);

        return JPA.em().createQuery(query).getResultList();
    }
}
