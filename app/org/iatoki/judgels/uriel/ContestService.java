package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ContestService {

    Contest findContestById(long contestId);

    Contest findContestByJid(String contestJid);

    void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard);

    Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin);

    boolean isThereNewProblemsOrContestants(String contestJid, long lastTime);

    ContestScoreState getContestStateByJid(String contestJid);

    List<Contest> getRunningContests(Date timeNow);

    Page<ContestAnnouncement> pageContestAnnouncementsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId);

    void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status);

    void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status);

    List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid);

    Map<String, String> findProblemJidToAliasMapByContestJid(String contestJid);

    Page<ContestProblem> pageContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    ContestProblem findContestProblemByContestProblemId(long contestProblemId);

    ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid);

    boolean isContestProblemInContestByProblemJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias);

    void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    void updateContestProblem(long contestProblemId, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String sortBy, String order, String filterString, List<String> askerJids);

    ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId);

    void createContestClarification(long contestId, String title, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String title, String question);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    long getUnansweredContestClarificationsCount(String contestJid);

    List<ContestContestant> findAllContestContestantsByContestJid(String contestJid);

    Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId);

    ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid);

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    long getContestContestantCount(String contestJid);

    void enterContestAsContestant(String contestJid, String userJid);

    void enterContestAsCoach(String contestJid, String coachJid);

    boolean isContestEntered(String contestJid, String contestContestantJid);

    Page<ContestTeam> pageContestTeamsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    List<ContestTeam> findAllContestTeams(String contestJid);

    ContestTeam findContestTeamByContestTeamId(long contestTeamId);

    void createContestTeam(long contestId, String name);

    void createContestTeam(long contestId, String name, File teamImage, String extension);

    void updateContestTeam(long contestTeamId, String name);

    void updateContestTeam(long contestTeamId, String name, File teamImage, String extension);

    boolean isUserInAnyTeamByContestJid(String contestJid, String userJid);

    boolean isUserCoachInAnyTeamByContestJid(String contestJid, String coachJid);

    ContestTeam findContestTeamJidByContestJidAndCoachJid(String contestJid, String coachJid);

    ContestTeamCoach findContestTeamCoachByContestTeamCoachId(long contestTeamCoachId);

    List<ContestTeamCoach> findContestTeamCoachesByTeamJid(String contestTeamJid);

    void createContestTeamCoach(String contestTeamJid, String coachJid);

    void removeContestTeamCoachByContestTeamCoachId(long contestTeamCoachId);

    ContestTeamMember findContestTeamMemberByContestTeamMemberId(long contestTeamMemberId);

    List<ContestTeamMember> findContestTeamMembersByContestJidAndCoachJid(String contestJid, String coachJid);

    List<ContestTeamMember> findContestTeamMembersByTeamJid(String contestTeamJid);

    void createContestTeamMember(String contestTeamJid, String memberJid);

    void removeContestTeamMemberByContestTeamMemberId(long contestTeamMemberId);

    ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid);

    Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestSupervisor findContestSupervisorByContestSupervisorId(long contestSupervisorId);

    boolean isContestSupervisorInContestByUserJid(String contestJid, String contestSupervisorJid);

    void createContestSupervisor(long contestId, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    void updateContestSupervisor(long contestSupervisorId, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestManager findContestManagerByContestManagerId(long contestManagerId);

    boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid);

    void createContestManager(long contestId, String userJid);

    long getUnreadContestAnnouncementsCount(String userJid, String contestJid);

    long getUnreadContestClarificationsCount(String userJid, String contestJid);

    void readContestAnnouncements(String userJid, List<Long> contestAnnouncementIds);

    void readContestClarifications(String userJid, List<Long> contestClarificationIds);

    boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type);

    ContestScoreboard findContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type);

    void upsertFrozenScoreboard(long contestScoreboardId);

    Map<String, URL> getMapContestantJidToImageUrlInContest(String contestJid);

    void updateContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type, Scoreboard scoreboard);

    ContestConfiguration findContestConfigurationByContestJid(String contestJid);

    void updateContestConfigurationByContestJid(String contestJid, ContestTypeConfig typeConfig, ContestScopeConfig scopeConfig, ContestStyleConfig styleConfig);

    String getTeamAvatarImageURL(String imageName);
}