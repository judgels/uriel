package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;

import java.util.List;

public interface ContestProblemDao extends Dao<Long, ContestProblemModel> {

    List<ContestProblemModel> findByContestJid(String contestJid);

    ContestProblemModel findByProblemJidOrderedByAlias(String contestJid, String problemJid);

    boolean existsByProblemJid(String contestJid, String problemJid);

    boolean existsByProblemAlias(String contestJid, String problemAlias);

    List<ContestProblemModel> findOpenedByContestJidOrderedByAlias(String contestJid);

    long countValidByContestJid(String contestJid);

    List<ContestProblemModel> findUsedByContestJid(String contestJid, long offset, long limit);

    boolean isThereNewProblem(String contestJid, long lastTime);
}
