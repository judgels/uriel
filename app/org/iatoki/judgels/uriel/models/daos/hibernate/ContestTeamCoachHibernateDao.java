package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamCoachModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamCoachModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestTeamCoachHibernateDao extends AbstractHibernateDao<Long, ContestTeamCoachModel> implements ContestTeamCoachDao {
    public ContestTeamCoachHibernateDao() {
        super(ContestTeamCoachModel.class);
    }

    @Override
    public boolean isUserRegisteredAsCoachInAnyTeam(String userJid, List<String> teamJids) {
        if (teamJids.isEmpty()) {
            return false;
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestTeamCoachModel> root = query.from(ContestTeamCoachModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestTeamCoachModel_.coachJid), userJid), root.get(ContestTeamCoachModel_.teamJid).in(teamJids)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestTeamCoachModel> findContestTeamCoachesInTeam(String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamCoachModel> query = cb.createQuery(ContestTeamCoachModel.class);
        Root<ContestTeamCoachModel> root = query.from(ContestTeamCoachModel.class);

        query.where(cb.equal(root.get(ContestTeamCoachModel_.teamJid), teamJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestTeamCoachModel findContestTeamCoachByCoachJidInAnyTeam(String userJid, List<String> teamJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamCoachModel> query = cb.createQuery(ContestTeamCoachModel.class);
        Root<ContestTeamCoachModel> root = query.from(ContestTeamCoachModel.class);

        query.where(cb.and(cb.equal(root.get(ContestTeamCoachModel_.coachJid), userJid), root.get(ContestTeamCoachModel_.teamJid).in(teamJids)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
