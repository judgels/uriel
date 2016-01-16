package org.iatoki.judgels.uriel.contest.clarification;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ContestClarificationHibernateDao.class)
public interface ContestClarificationDao extends JudgelsDao<ContestClarificationModel> {

    long countUnansweredInContest(String contestJid);

    List<String> getAnsweredJidsInContestAskedByUsers(String contestJid, Collection<String> userJids);

    long countInContestAskedByUsers(String contestJid, Collection<String> userJids);

    List<ContestClarificationModel> getAllInContestAskedByUsers(String contestJid, Collection<String> userJids);

    List<String> getJidsInContestAskedByUsers(String contestJid, Collection<String> userJids);
}
