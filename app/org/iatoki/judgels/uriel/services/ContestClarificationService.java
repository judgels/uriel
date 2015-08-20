package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationNotFoundException;
import org.iatoki.judgels.uriel.ContestClarificationStatus;

import java.util.Collection;
import java.util.List;

public interface ContestClarificationService {

    ContestClarification findContestClarificationById(long contestClarificationId) throws ContestClarificationNotFoundException;

    Page<ContestClarification> getPageOfClarificationsInContest(String contestJid, long pageIndex, long pageSize, String sortBy, String order, String filterString, List<String> askerJids);

    long countUnansweredClarificationsInContest(String contestJid);

    long countUnreadClarificationsInContest(Collection<String> askerJids, String userJid, String contestJid, boolean answered);

    void createContestClarification(long contestId, String title, String question, String topicJid);

    void updateContestClarification(long contestClarificationId, String title, String question);

    void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status);

    void readContestClarifications(String userJid, Collection<String> contestClarificationJids);
}
