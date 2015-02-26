package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamDao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestTeamHibernateDao extends AbstractJudgelsHibernateDao<ContestTeamModel> implements ContestTeamDao {
    public ContestTeamHibernateDao() {
        super(ContestTeamModel.class);
    }

    @Override
    public List<String> findAllTeamJidsInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestTeamModel> root = query.from(ContestTeamModel.class);

        query.select(root.get(ContestTeamModel_.jid)).where(cb.equal(root.get(ContestTeamModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
