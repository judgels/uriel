package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestProblemDao")
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
                .where(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid))
                .orderBy(cb.asc(root.get(ContestProblemModel_.alias)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestProblemModel findByProblemJidOrderedByAlias(String contestJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.problemJid), problemJid), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

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
    public boolean existsByProblemAlias(String contestJid, String problemAlias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.alias), problemAlias), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

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

    @Override
    public List<ContestProblemModel> findUsedByContestJidOrderedByAlias(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        Order orderBy = cb.asc(root.get(ContestProblemModel_.alias));

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.notEqual(root.get(ContestProblemModel_.status), ContestProblemStatus.UNUSED.name())))
                .orderBy(orderBy);

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public long countValidByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.notEqual(root.get(ContestProblemModel_.status), ContestProblemStatus.UNUSED.name())));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestProblemModel> findUsedByContestJidOrderedByStatusAndThenAlias(String contestJid, long offset, long limit) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.notEqual(root.get(ContestProblemModel_.status), ContestProblemStatus.UNUSED.name())))
                .orderBy(cb.desc(root.get(ContestProblemModel_.status)), cb.asc(root.get(ContestProblemModel_.alias)));

        TypedQuery<ContestProblemModel> q = JPA.em().createQuery(query).setFirstResult((int) offset);
        if (limit != -1) {
            q.setMaxResults((int) limit);
        }
        return q.getResultList();
    }

    @Override
    public boolean isThereNewProblem(String contestJid, long lastTime) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.gt(root.get(ContestProblemModel_.timeUpdate), lastTime)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }
}
