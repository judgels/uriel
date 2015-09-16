package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestSupervisorDao")
public final class ContestSupervisorJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestSupervisorModel> implements ContestSupervisorDao {

    @Inject
    public ContestSupervisorJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestSupervisorModel.class);
    }

    @Override
    public boolean existsInContestByJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid), cb.equal(root.get(ContestSupervisorModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestSupervisorModel findInContestByJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestSupervisorModel> query = cb.createQuery(getModelClass());
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
                .where(cb.and(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid), cb.equal(root.get(ContestSupervisorModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> getContestJidsByJid(String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
            .select(root.get(ContestSupervisorModel_.contestJid))
            .where(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
