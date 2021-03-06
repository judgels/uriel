package org.iatoki.judgels.uriel.contest.supervisor;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ContestSupervisorHibernateDao extends AbstractHibernateDao<Long, ContestSupervisorModel> implements ContestSupervisorDao {

    public ContestSupervisorHibernateDao() {
        super(ContestSupervisorModel.class);
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
