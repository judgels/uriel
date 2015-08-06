package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestStyleDao;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("contestModuleDao")
public final class ContestStyleHibernateDao extends AbstractHibernateDao<Long, ContestStyleModel> implements ContestStyleDao {

    public ContestStyleHibernateDao() {
        super(ContestStyleModel.class);
    }

    @Override
    public boolean isExistByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestStyleModel> root = query.from(ContestStyleModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ContestStyleModel_.contestJid), contestJid));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestStyleModel findByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestStyleModel> query = cb.createQuery(ContestStyleModel.class);
        Root<ContestStyleModel> root = query.from(ContestStyleModel.class);

        query.where(cb.equal(root.get(ContestStyleModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
