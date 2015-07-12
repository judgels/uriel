package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;

import java.util.List;

public interface ContestSupervisorDao extends Dao<Long, ContestSupervisorModel> {

    boolean existsByContestJidAndSupervisorJid(String contestJid, String supervisorJid);

    ContestSupervisorModel findByContestJidAndSupervisorJid(String contestJid, String supervisorJid);

    List<String> findContestJidsBySupervisorJid(String supervisorJid);
}
