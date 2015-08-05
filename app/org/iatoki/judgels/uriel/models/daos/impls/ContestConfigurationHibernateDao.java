package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestConfigurationDao;
import org.iatoki.judgels.uriel.models.entities.ContestConfigurationModel;
import org.iatoki.judgels.uriel.models.entities.ContestConfigurationModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("contestConfigurationDao")
public final class ContestConfigurationHibernateDao extends AbstractHibernateDao<Long, ContestConfigurationModel> implements ContestConfigurationDao {

    public ContestConfigurationHibernateDao() {
        super(ContestConfigurationModel.class);
    }

    @Override
    public boolean isExistByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestConfigurationModel> root = query.from(ContestConfigurationModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ContestConfigurationModel_.contestJid), contestJid));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestConfigurationModel findByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestConfigurationModel> query = cb.createQuery(ContestConfigurationModel.class);
        Root<ContestConfigurationModel> root = query.from(ContestConfigurationModel.class);

        query.where(cb.equal(root.get(ContestConfigurationModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
