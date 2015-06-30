package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationNotFoundException;
import org.iatoki.judgels.uriel.ContestClarificationStatus;

import java.util.List;

public interface ContestClarificationService {

    ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId) throws ContestClarificationNotFoundException;

    Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String sortBy, String order, String filterString, List<String> askerJids);

    long getUnansweredContestClarificationsCount(String contestJid);

    long getUnreadContestClarificationsCount(List<String> askerJids, String userJid, String contestJid, boolean answered);

    void createContestClarification(long contestId, String title, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String title, String question);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    void readContestClarifications(String userJid, List<Long> contestClarificationIds);
}
