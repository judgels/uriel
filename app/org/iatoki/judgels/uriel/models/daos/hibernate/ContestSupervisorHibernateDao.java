package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

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
