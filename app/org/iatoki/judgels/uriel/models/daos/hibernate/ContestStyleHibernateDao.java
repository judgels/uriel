package org.iatoki.judgels.uriel.models.daos.hibernate;

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
    public ContestStyleModel findInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestStyleModel> query = cb.createQuery(ContestStyleModel.class);
        Root<ContestStyleModel> root = query.from(ContestStyleModel.class);

        query.where(cb.equal(root.get(ContestStyleModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
