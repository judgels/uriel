package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;

import java.util.Collection;
import java.util.List;

public interface ContestClarificationDao extends JudgelsDao<ContestClarificationModel> {

    long countUnansweredClarificationByContestJid(String contestJid);

    List<String> findAllAnsweredClarificationJidsInContestByUserJids(String contestJid, Collection<String> userJids);

    long countClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<ContestClarificationModel> findClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<String> findClarificationJidsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);
}
