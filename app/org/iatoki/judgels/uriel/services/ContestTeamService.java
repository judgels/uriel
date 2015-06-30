package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.commons.Page;
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

    boolean isUserInAnyTeamByContestJid(String contestJid, String userJid);

    boolean isUserCoachInAnyTeamByContestJid(String contestJid, String coachJid);

    boolean isUserCoachByUserJidAndTeamJid(String coachJid, String teamJid);

    ContestTeam findContestTeamByContestTeamId(long contestTeamId) throws ContestTeamNotFoundException;

    ContestTeamCoach findContestTeamCoachByContestTeamCoachId(long contestTeamCoachId) throws ContestTeamCoachNotFoundException;

    ContestTeamMember findContestTeamMemberByContestTeamMemberId(long contestTeamMemberId) throws ContestTeamMemberNotFoundException;

    Page<ContestTeam> pageContestTeamsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ContestTeam> pageContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid, long pageIndex, long pageSize, String orderBy, String orderDir);

    List<ContestTeam> findAllContestTeams(String contestJid);

    List<ContestTeam> findContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid);

    List<ContestTeamCoach> findContestTeamCoachesByTeamJid(String contestTeamJid);

    List<ContestTeamMember> findContestTeamMembersByTeamJid(String contestTeamJid);

    List<ContestTeamMember> findContestTeamMembersByContestJidAndCoachJid(String contestJid, String coachJid);

    String getTeamAvatarImageURL(String imageName);

    void createContestTeam(long contestId, String name);

    void createContestTeam(long contestId, String name, File teamImage, String extension) throws IOException;

    void updateContestTeam(long contestTeamId, String name);

    void updateContestTeam(long contestTeamId, String name, File teamImage, String extension) throws IOException;

    void createContestTeamCoach(String contestTeamJid, String coachJid);

    void removeContestTeamCoachByContestTeamCoachId(long contestTeamCoachId);

    void createContestTeamMember(String contestTeamJid, String memberJid);

    void removeContestTeamMemberByContestTeamMemberId(long contestTeamMemberId);

    void startTeamAsCoach(String contestJid, String teamJid);
}
