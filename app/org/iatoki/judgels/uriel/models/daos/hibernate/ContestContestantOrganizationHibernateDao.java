package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantOrganizationDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantOrganizationModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantOrganizationModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("ContestContestantOrganizationDao")
public class ContestContestantOrganizationHibernateDao extends AbstractHibernateDao<Long, ContestContestantOrganizationModel> implements ContestContestantOrganizationDao {

    public ContestContestantOrganizationHibernateDao() {
        super(ContestContestantOrganizationModel.class);
    }

    @Override
    public ContestContestantOrganizationModel findInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantOrganizationModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantOrganizationModel> root = query.from(getModelClass());

        query.where(cb.and(cb.equal(root.get(ContestContestantOrganizationModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantOrganizationModel_.userJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
