package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel;

import java.util.Collection;
import java.util.List;

public interface ContestTeamMemberDao extends Dao<Long, ContestTeamMemberModel> {

    boolean isUserRegisteredAsMemberInAnyTeam(String userJid, List<String> teamJids);

    List<ContestTeamMemberModel> getAllInTeam(String teamJid);

    List<ContestTeamMemberModel> getAllInTeams(Collection<String> teamJids);

    ContestTeamMemberModel findByJidInAnyTeam(String userJid, Collection<String> teamJids);
}
