package org.iatoki.judgels.uriel.contest.problem;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ContestProblemJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestProblemModel> implements ContestProblemDao {

    @Inject
    public ContestProblemJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestProblemModel.class);
    }

    @Override
    public List<ContestProblemModel> getAllInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .where(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid))
                .orderBy(cb.asc(root.get(ContestProblemModel_.alias)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestProblemModel findInContestByJid(String contestJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestProblemModel> query = cb.createQuery(ContestProblemModel.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.problemJid), problemJid), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean existsInContestByJid(String contestJid, String problemJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.problemJid), problemJid), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsInContestByAlias(String contestJid, String problemAlias) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.alias), problemAlias), cb.equal(root.get(ContestProblemModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestProblemModel> getOpenedInContest(String contestJid) {
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
    public List<ContestProblemModel> getUsedInContest(String contestJid) {
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
    public long countValidInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestProblemModel> root = query.from(ContestProblemModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestProblemModel_.contestJid), contestJid), cb.notEqual(root.get(ContestProblemModel_.status), ContestProblemStatus.UNUSED.name())));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestProblemModel> getUsedInContestWithLimit(String contestJid, long offset, long limit) {
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
}
