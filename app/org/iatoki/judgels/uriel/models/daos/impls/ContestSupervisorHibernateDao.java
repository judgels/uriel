package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestSupervisorDao")
public final class ContestSupervisorHibernateDao extends AbstractHibernateDao<Long, ContestSupervisorModel> implements ContestSupervisorDao {

    public ContestSupervisorHibernateDao() {
        super(ContestSupervisorModel.class);
    }

    @Override
    public boolean existsByContestJidAndSupervisorJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid), cb.equal(root.get(ContestSupervisorModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestSupervisorModel findByContestJidAndSupervisorJid(String contestJid, String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestSupervisorModel> query = cb.createQuery(getModelClass());
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
                .where(cb.and(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid), cb.equal(root.get(ContestSupervisorModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findContestJidsBySupervisorJid(String supervisorJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestSupervisorModel> root = query.from(getModelClass());

        query
            .select(root.get(ContestSupervisorModel_.contestJid))
            .where(cb.equal(root.get(ContestSupervisorModel_.userJid), supervisorJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
