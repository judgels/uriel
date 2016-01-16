package org.iatoki.judgels.uriel.contest.team.member;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
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
    public List<ContestTeamMemberModel> getAllInTeam(String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.equal(root.get(ContestTeamMemberModel_.teamJid), teamJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestTeamMemberModel> getAllInTeams(Collection<String> teamJids) {
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
    public ContestTeamMemberModel findByJidInAnyTeam(String userJid, Collection<String> teamJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.and(cb.equal(root.get(ContestTeamMemberModel_.memberJid), userJid), root.get(ContestTeamMemberModel_.teamJid).in(teamJids)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
