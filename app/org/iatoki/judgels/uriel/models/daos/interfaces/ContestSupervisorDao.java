package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel;

public interface ContestSupervisorDao extends Dao<Long, ContestSupervisorModel> {

    boolean existsBySupervisorJid(String contestJid, String supervisorJid);

    ContestSupervisorModel findBySupervisorJid(String contestId, String supervisorJid);
}
