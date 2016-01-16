package org.iatoki.judgels.uriel.contest.team.coach;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ContestTeamCoachHibernateDao.class)
public interface ContestTeamCoachDao extends Dao<Long, ContestTeamCoachModel> {

    boolean isUserRegisteredAsCoachInTeams(String userJid, List<String> teamJids);

    List<ContestTeamCoachModel> getAllByTeamJid(String teamJid);

    List<ContestTeamCoachModel> getAllInTeamsByJid(String coachJid, List<String> teamJids);

    List<String> getTeamJidsByJid(String coachJid);

    boolean isUserCoachByUserJidAndTeamJid(String userJid, String teamJid);
}
