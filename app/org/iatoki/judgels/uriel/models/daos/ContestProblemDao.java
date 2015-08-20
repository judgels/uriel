package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;

import java.util.List;

public interface ContestProblemDao extends Dao<Long, ContestProblemModel> {

    List<ContestProblemModel> getAllInContest(String contestJid);

    ContestProblemModel findInContestByJid(String contestJid, String problemJid);

    boolean existsInContestByJid(String contestJid, String problemJid);

    boolean existsInContestByAlias(String contestJid, String problemAlias);

    List<ContestProblemModel> getOpenedInContest(String contestJid);

    List<ContestProblemModel> getUsedInContest(String contestJid);

    long countValidInContest(String contestJid);

    List<ContestProblemModel> getUsedInContestWithLimit(String contestJid, long offset, long limit);
}
