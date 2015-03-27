package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel;

import java.util.List;

public interface ContestSupervisorDao extends Dao<Long, ContestSupervisorModel> {

    boolean existsByContestJidAndSupervisorJid(String contestJid, String supervisorJid);

    ContestSupervisorModel findByContestJidAndSupervisorJid(String contestJid, String supervisorJid);

    List<String> findContestJidsBySupervisorJid(String supervisorJid);
}
