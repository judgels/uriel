package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamMemberModel;

import java.util.List;

public interface ContestTeamMemberDao extends Dao<Long, ContestTeamMemberModel> {

    boolean isUserRegisteredAsMemberInAnyTeam(String userJid, List<String> teamJids);

    List<ContestTeamMemberModel> findContestTeamMembersInTeam(String teamJid);

    List<ContestTeamMemberModel> findContestTeamMembersInTeams(List<String> teamJids);

    ContestTeamMemberModel findContestTeamMemberByMemberJidInAnyTeam(String userJid, List<String> teamJids);
}
