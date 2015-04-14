package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;

import java.util.Collection;
import java.util.List;

public interface ContestClarificationDao extends Dao<Long, ContestClarificationModel> {

    long countUnansweredClarificationByContestJid(String contestJid);

    List<Long> findAllAnsweredClarificationIdsInContestByUserJids(String contestJid, Collection<String> userJids);

    long countClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<ContestClarificationModel> findClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);

    List<Long> findClarificationIdsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids);
}
