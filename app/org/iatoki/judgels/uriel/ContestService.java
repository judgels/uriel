package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;

import java.util.Date;
import java.util.List;

public interface ContestService {

    Contest findContestById(long contestId);

    Contest findContestByJid(String contestJid);

    void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime);

    Page<Contest> pageContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ContestAnnouncement> pageContestAnnouncementsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId);

    void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status);

    void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status);

    List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid);

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

    Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId);

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid);

    Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestSupervisor findContestSupervisrByContestSupervisorId(long contestSupervisorId);

    boolean isContestSupervisorInContestByUserJid(String contestJid, String contestSupervisorJid);

    void createContestSupervisor(long contestId, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    void updateContestSupervisor(long contestSupervisorId, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ContestManager findContestManagerByContestManagerId(long contestManagerId);

    boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid);

    void createContestManager(long contestId, String userJid);

}