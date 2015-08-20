package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;

import java.util.Collection;
import java.util.List;

public interface ContestClarificationDao extends JudgelsDao<ContestClarificationModel> {

    long countUnansweredInContest(String contestJid);

    List<String> getAnsweredJidsInContestAskedByUsers(String contestJid, Collection<String> userJids);

    long countInContestAskedByUsers(String contestJid, Collection<String> userJids);

    List<ContestClarificationModel> getAllInContestAskedByUsers(String contestJid, Collection<String> userJids);

    List<String> getJidsInContestAskedByUsers(String contestJid, Collection<String> userJids);
}
