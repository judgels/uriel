package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestScoreDao;
import org.iatoki.judgels.uriel.models.domains.ContestScoreModel;
import org.iatoki.judgels.uriel.models.domains.ContestScoreModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestScoreHibernateDao extends AbstractHibernateDao<Long, ContestScoreModel> implements ContestScoreDao {
    public ContestScoreHibernateDao() {
        super(ContestScoreModel.class);
    }

    @Override
    public List<ContestScoreModel> findByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestScoreModel> query = cb.createQuery(ContestScoreModel.class);
        Root<ContestScoreModel> root = query.from(ContestScoreModel.class);

        query.where(cb.equal(root.get(ContestScoreModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
