package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamCoachModel;

import java.util.List;

public interface ContestTeamCoachDao extends Dao<Long, ContestTeamCoachModel> {

    boolean isUserRegisteredAsCoachInTeams(String userJid, List<String> teamJids);

    List<ContestTeamCoachModel> findContestTeamCoachesByTeamJid(String teamJid);

    ContestTeamCoachModel findContestTeamCoachByCoachJidInTeams(String coachJid, List<String> teamJids);

    List<String> findContestTeamJidsByCoachJid(String coachJid);

}
