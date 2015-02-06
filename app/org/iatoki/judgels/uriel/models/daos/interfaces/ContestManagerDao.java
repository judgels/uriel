package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;

import java.util.List;

public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean isExistByManagerJid(String contestJid, String managerJid);

    ContestManagerModel findByManagerJid(String contestId, String managerJid);

    long countByFilter(String contestJid, String filterString, List<String> userJids);

    List<ContestManagerModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max);
}
