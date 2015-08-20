package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamCoachModel;

import java.util.List;

public interface ContestTeamCoachDao extends Dao<Long, ContestTeamCoachModel> {

    boolean isUserRegisteredAsCoachInTeams(String userJid, List<String> teamJids);

    List<ContestTeamCoachModel> getAllByTeamJid(String teamJid);

    List<ContestTeamCoachModel> getAllInTeamsByJid(String coachJid, List<String> teamJids);

    List<String> getTeamJidsByJid(String coachJid);

    boolean isUserCoachByUserJidAndTeamJid(String userJid, String teamJid);
}
