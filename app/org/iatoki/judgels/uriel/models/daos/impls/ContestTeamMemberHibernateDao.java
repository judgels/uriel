package org.iatoki.judgels.uriel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestTeamMemberHibernateDao extends AbstractHibernateDao<Long, ContestTeamMemberModel> implements ContestTeamMemberDao {
    public ContestTeamMemberHibernateDao() {
        super(ContestTeamMemberModel.class);
    }

    @Override
    public boolean isUserRegisteredAsMemberInAnyTeam(String userJid, List<String> teamJids) {
        if (teamJids.isEmpty()) {
            return false;
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestTeamMemberModel_.memberJid), userJid), root.get(ContestTeamMemberModel_.teamJid).in(teamJids)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestTeamMemberModel> findContestTeamMembersInTeam(String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.equal(root.get(ContestTeamMemberModel_.teamJid), teamJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestTeamMemberModel> findContestTeamMembersInTeams(List<String> teamJids) {
        if (teamJids.isEmpty()) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(root.get(ContestTeamMemberModel_.teamJid).in(teamJids));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestTeamMemberModel findContestTeamMemberByMemberJidInAnyTeam(String userJid, List<String> teamJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.and(cb.equal(root.get(ContestTeamMemberModel_.memberJid), userJid), root.get(ContestTeamMemberModel_.teamJid).in(teamJids)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
