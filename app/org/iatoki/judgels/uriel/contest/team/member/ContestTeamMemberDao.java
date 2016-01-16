package org.iatoki.judgels.uriel.contest.team.member;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ContestTeamMemberHibernateDao.class)
public interface ContestTeamMemberDao extends Dao<Long, ContestTeamMemberModel> {

    boolean isUserRegisteredAsMemberInAnyTeam(String userJid, List<String> teamJids);

    List<ContestTeamMemberModel> getAllInTeam(String teamJid);

    List<ContestTeamMemberModel> getAllInTeams(Collection<String> teamJids);

    ContestTeamMemberModel findByJidInAnyTeam(String userJid, Collection<String> teamJids);
}
