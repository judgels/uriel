package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ContestService {

    Contest findContestById(long contestId);

    Contest findContestByJid(String contestJid);

    void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isIncognitoScoreboard);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isIncognitoScoreboard);

    Page<Contest> pageContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    boolean isThereNewProblemsOrContestants(String contestJid, long lastTime);

    ContestScoreState getContestConfigByJid(String contestJid);

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

    boolean isContestProblemInContestByProblemJid(String contestJid, String contestProblemJid);

    void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    void updateContestProblem(long contestProblemId, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String sortBy, String order, String filterString, String askerJid);

    ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId);

    void createContestClarification(long contestId, String title, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    long getUnansweredContestClarificationsCount(String contestJid);

    Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId);

    ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid);

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    long getContestContestantCount(String contestJid);

    void enterContest(String contestJid, String contestContestantJid);

    boolean isContestEntered(String contestJid, String contestContestantJid);

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

    ContestScoreboard findContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type);

    List<ContestScore> findContestScoresInContest(String contestJid, ScoreAdapter adapter);

    void updateContestScoreBySubmissions(String contestJid, List<Submission> submissions, ScoreAdapter adapter, ContestScoreState contestScoreState);

    void updateContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type, Scoreboard scoreboard);

    ContestConfiguration findContestConfigurationByContestJid(String contestJid);

    void updateContestConfigurationByContestJid(String contestJid, ContestTypeConfig typeConfig, ContestScopeConfig scopeConfig, ContestStyleConfig styleConfig);
}