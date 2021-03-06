package org.iatoki.judgels.uriel.contest.module;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ContestModuleHibernateDao extends AbstractHibernateDao<Long, ContestModuleModel> implements ContestModuleDao {

    public ContestModuleHibernateDao() {
        super(ContestModuleModel.class);
    }

    @Override
    public boolean existsEnabledInContestByName(String contestJid, String contestModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestModuleModel> root = query.from(ContestModuleModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestModuleModel_.contestJid), contestJid), cb.equal(root.get(ContestModuleModel_.name), contestModuleName), cb.equal(root.get(ContestModuleModel_.enabled), true)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public boolean existsInContestByName(String contestJid, String contestModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestModuleModel> root = query.from(ContestModuleModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestModuleModel_.contestJid), contestJid), cb.equal(root.get(ContestModuleModel_.name), contestModuleName)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public ContestModuleModel findInContestByName(String contestJid, String contestModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModuleModel> query = cb.createQuery(ContestModuleModel.class);
        Root<ContestModuleModel> root = query.from(ContestModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ContestModuleModel_.contestJid), contestJid), cb.equal(root.get(ContestModuleModel_.name), contestModuleName)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestModuleModel> getEnabledInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModuleModel> query = cb.createQuery(ContestModuleModel.class);
        Root<ContestModuleModel> root = query.from(ContestModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ContestModuleModel_.contestJid), contestJid), cb.equal(root.get(ContestModuleModel_.enabled), true)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestModuleModel> getEnabledByName(String contestModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModuleModel> query = cb.createQuery(ContestModuleModel.class);
        Root<ContestModuleModel> root = query.from(ContestModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ContestModuleModel_.name), contestModuleName), cb.equal(root.get(ContestModuleModel_.enabled), true)));

        return JPA.em().createQuery(query).getResultList();
    }
}
