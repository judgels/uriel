package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;

import java.util.Collection;
import java.util.List;

public interface ContestClarificationDao extends Dao<Long, ContestClarificationModel> {

    long countUnansweredClarificationByContestJid(String contestJid);

    List<Long> findAllAnsweredClarificationIdsInContestByUserJids(String contestJid, Collection<String> userJids);

    long countClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<ContestClarificationModel> findClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<Long> findClarificationIdsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);
}
