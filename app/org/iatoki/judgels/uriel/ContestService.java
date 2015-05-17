package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.FileInfo;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ContestService {

    Contest findContestById(long contestId) throws ContestNotFoundException;

    Contest findContestByJid(String contestJid);

    Contest createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword);

    Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin);

    boolean isThereNewProblemsOrContestants(String contestJid, long lastTime);

    ContestScoreState getContestStateByJid(String contestJid);

    List<Contest> getRunningContests(Date timeNow);

    Page<ContestAnnouncement> pageContestAnnouncementsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId) throws ContestAnnouncementNotFoundException;

    void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status);

    void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status);

    List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid);

    Map<String, String> findProblemJidToAliasMapByContestJid(String contestJid);

    Page<ContestProblem> pageContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    Page<ContestProblem> pageUsedContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize);

    ContestProblem findContestProblemByContestProblemId(long contestProblemId) throws ContestProblemNotFoundException;

    ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid);

    boolean isContestProblemInContestByProblemJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias);

    void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    void updateContestProblem(long contestProblemId, String alias, long submissionsLimit, ContestProblemStatus status);

    Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String sortBy, String order, String filterString, List<String> askerJids);

    ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId) throws ContestClarificationNotFoundException;

    void createContestClarification(long contestId, String title, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String title, String question);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    long getUnansweredContestClarificationsCount(String contestJid);

    List<ContestContestant> findAllContestContestantsByContestJid(String contestJid);

    Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId) throws ContestContestantNotFoundException;

    ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid);

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    void deleteContestContestant(long contestContestantId);

    long getContestContestantCount(String contestJid);

    void startContestAsContestant(String contestJid, String userJid);

    void startTeamAsCoach(String contestJid, String teamJid);

    boolean isContestStarted(String contestJid, String contestContestantJid);

    void generateContestantPasswordForAllContestants(String contestJid);

    void generateContestantPassword(String contestJid, String contestantJid);

    String getContestantPassword(String contestJid, String contestantJid);

    Map<String, String> getContestantPasswordsMap(String contestJid, Collection<String> contestantJids);

    Page<ContestTeam> pageContestTeamsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ContestTeam> pageContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid, long pageIndex, long pageSize, String orderBy, String orderDir);

    List<ContestTeam> findAllContestTeams(String contestJid);

    ContestTeam findContestTeamByContestTeamId(long contestTeamId) throws ContestTeamNotFoundException;

    void createContestTeam(long contestId, String name);

    void createContestTeam(long contestId, String name, File teamImage, String extension) throws IOException;

    void updateContestTeam(long contestTeamId, String name);

    void updateContestTeam(long contestTeamId, String name, File teamImage, String extension) throws IOException;

    boolean isUserInAnyTeamByContestJid(String contestJid, String userJid);

    boolean isUserCoachInAnyTeamByContestJid(String contestJid, String coachJid);

    boolean isUserCoachByUserJidAndTeamJid(String coachJid, String teamJid);

    List<ContestTeam> findContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid);

    ContestTeamCoach findContestTeamCoachByContestTeamCoachId(long contestTeamCoachId) throws ContestTeamCoachNotFoundException;

    List<ContestTeamCoach> findContestTeamCoachesByTeamJid(String contestTeamJid);

    void createContestTeamCoach(String contestTeamJid, String coachJid);

    void removeContestTeamCoachByContestTeamCoachId(long contestTeamCoachId);

    ContestTeamMember findContestTeamMemberByContestTeamMemberId(long contestTeamMemberId) throws ContestTeamMemberNotFoundException;

    List<ContestTeamMember> findContestTeamMembersByContestJidAndCoachJid(String contestJid, String coachJid);

    List<ContestTeamMember> findContestTeamMembersByTeamJid(String contestTeamJid);

    void createContestTeamMember(String contestTeamJid, String memberJid);

    void removeContestTeamMemberByContestTeamMemberId(long contestTeamMemberId);

    ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid);

    Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestSupervisor findContestSupervisorByContestSupervisorId(long contestSupervisorId) throws ContestSupervisorNotFoundException;

    boolean isContestSupervisorInContestByUserJid(String contestJid, String contestSupervisorJid);

    void createContestSupervisor(long contestId, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    void updateContestSupervisor(long contestSupervisorId, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid);

    void createContestManager(long contestId, String userJid);

    long getUnreadContestAnnouncementsCount(String userJid, String contestJid);

    long getUnreadContestClarificationsCount(List<String> askerJids, String userJid, String contestJid, boolean answered);

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

    List<FileInfo> getContestFiles(String contestJid);

    void uploadContestFile(String contestJid, File file, String filename) throws IOException;

    String getContestFileURL(String contestJid, String filename);
}