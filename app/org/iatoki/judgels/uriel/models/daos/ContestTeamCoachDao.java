package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamCoachModel;

import java.util.List;

public interface ContestTeamCoachDao extends Dao<Long, ContestTeamCoachModel> {

    boolean isUserRegisteredAsCoachInTeams(String userJid, List<String> teamJids);

    List<ContestTeamCoachModel> findContestTeamCoachesByTeamJid(String teamJid);

    List<ContestTeamCoachModel> findContestTeamCoachesByCoachJidInTeams(String coachJid, List<String> teamJids);

    List<String> findContestTeamJidsByCoachJid(String coachJid);

    boolean isUserCoachByUserJidAndTeamJid(String userJid, String teamJid);

}
