package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;

import java.util.Date;
import java.util.List;

public interface ContestService {

    Contest findContestById(long contestId);

    Contest findContestByJid(String contestJid);

    void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime);

    void deleteContest(long contestId);

    Page<Contest> pageContest(long page, long pageSize, String sortBy, String order, String filterString);

    List<ContestAnnouncement> findPublishedContestAnnouncementByContestJid(String contestJid);

    List<ContestAnnouncement> findContestAnnouncementByContestJid(String contestJid, String sortBy, String order, String filterString);

    ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId);

    void createContestAnnouncement(long contestId, String title, String announcement, ContestAnnouncementStatus status);

    void updateContestAnnouncement(long contestAnnouncementId, String title, String announcement, ContestAnnouncementStatus status);

    List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid);

    Page<ContestProblem> pageContestProblemByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString);

    ContestProblem findContestProblemByContestProblemId(long contestProblemId);

    boolean isContestProblemInContestByProblemJid(String contestJid, String contestProblemJid);

    void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, String name, long submissionLimit, ContestProblemStatus status);

    void updateContestProblem(long contestProblemId, String problemSecret, String alias, String name, long submissionLimit, ContestProblemStatus status);

    List<ContestClarification> findContestClarificationByContestJidAndAskerJid(String contestJid, String askerJid);

    Page<ContestClarification> pageContestClarificationByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString);

    ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId);

    void createContestClarification(long contestId, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    Page<ContestContestant> pageContestContestantByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId);

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

}