package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ContestTeamMemberNotFoundException;
import org.iatoki.judgels.uriel.ContestTeamNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    void createContestTeam(long contestId, String name);

    void createContestTeam(long contestId, String name, File teamImage, String extension) throws IOException;

    void updateContestTeam(long contestTeamId, String name);

    void updateContestTeam(long contestTeamId, String name, File teamImage, String extension) throws IOException;

    void createContestTeamCoach(String contestTeamJid, String coachJid);

    void removeContestTeamCoachById(long contestTeamCoachId);

    void createContestTeamMember(String contestTeamJid, String memberJid);

    void removeContestTeamMemberById(long contestTeamMemberId);

    void startTeamAsCoach(String contestJid, String teamJid);
}
