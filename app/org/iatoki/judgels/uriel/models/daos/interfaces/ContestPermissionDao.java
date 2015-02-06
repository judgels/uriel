package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestPermissionModel;

import java.util.List;

public interface ContestPermissionDao extends Dao<Long, ContestPermissionModel> {

    boolean isExistBySupervisorJid(String contestJid, String supervisorJid);

    ContestPermissionModel findBySupervisorJid(String contestId, String supervisorJid);

    long countByFilter(String contestJid, String filterString, List<String> userJids);

    List<ContestPermissionModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order, long first, long max);

}
