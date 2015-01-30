package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;

import java.util.List;

public interface ContestProblemDao extends Dao<Long, ContestProblemModel> {

    List<ContestProblemModel> findByContestJid(String contestJid);

    ContestProblemModel findByProblemJid(String contestJid, String problemJid);

    boolean isExistByProblemJid(String contestJid, String problemJid);

    List<ContestProblemModel> findOpenedByContestJidOrderedByAlias(String contestJid);

    long countByFilter(String contestJid, String filterString);

    List<ContestProblemModel> findByContestJidFilterAndSort(String contestJid, String filterString, String sortBy, String order, long first, long max);


}
