package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;

import java.util.List;

public interface ContestClarificationDao extends Dao<Long, ContestClarificationModel> {

    List<ContestClarificationModel> findByContestJidAndAskerJid(String contestJid, String askerJid);

    long countByFilter(String contestJid, String filterString, List<String> userJids);

    List<ContestClarificationModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max);
}
