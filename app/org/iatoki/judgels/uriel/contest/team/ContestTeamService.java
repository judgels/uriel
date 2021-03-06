package org.iatoki.judgels.uriel.contest.team;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoach;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMember;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMemberNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@ImplementedBy(ContestTeamServiceImpl.class)
public interface ContestTeamService {

    boolean isUserPartOfAnyTeamInContest(String contestJid, String userJid);

    boolean isUserACoachOfAnyTeamInContest(String contestJid, String coachJid);

    boolean isUserACoachInTeam(String coachJid, String teamJid);

    ContestTeam findContestTeamById(long contestTeamId) throws ContestTeamNotFoundException;

    ContestTeamCoach findContestTeamCoachById(long contestTeamCoachId) throws ContestTeamCoachNotFoundException;

    ContestTeamMember findContestTeamMemberById(long contestTeamMemberId) throws ContestTeamMemberNotFoundException;

    Page<ContestTeam> getPageOfTeamsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ContestTeam> getPageOfTeamsInContestByCoachJid(String contestJid, String coachJid, long pageIndex, long pageSize, String orderBy, String orderDir);

    List<ContestTeam> getTeamsInContest(String contestJid);

    List<ContestTeam> getTeamsInContestByCoachJid(String contestJid, String coachJid);

    List<ContestTeamCoach> getCoachesOfTeam(String contestTeamJid);

    List<ContestTeamMember> getMembersOfTeam(String contestTeamJid);

    List<ContestTeamMember> getCoachedMembersInContest(String contestJid, String coachJid);

    String getTeamAvatarImageURL(String imageName);

    ContestTeam createContestTeam(String contestJid, String name, String userJid, String userIpAddress);

    ContestTeam createContestTeam(String contestJid, String name, File teamImage, String extension, String userJid, String userIpAddress) throws IOException;

    void updateContestTeam(String contestTeamJid, String name, String userJid, String userIpAddress);

    void updateContestTeam(String contestTeamJid, String name, File teamImage, String extension, String userJid, String userIpAddress) throws IOException;

    void createContestTeamCoach(String contestTeamJid, String coachJid, String userJid, String userIpAddress);

    void removeContestTeamCoachById(long contestTeamCoachId);

    void createContestTeamMember(String contestTeamJid, String memberJid, String userJid, String userIpAddress);

    void removeContestTeamMemberById(long contestTeamMemberId);

    void startTeamAsCoach(String contestJid, String teamJid, String userJid, String userIpAddress);
}
