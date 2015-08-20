package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;

import java.util.List;

public interface ContestSupervisorDao extends Dao<Long, ContestSupervisorModel> {

    boolean existsInContestByJid(String contestJid, String supervisorJid);

    ContestSupervisorModel findInContestByJid(String contestJid, String supervisorJid);

    List<String> getContestJidsByJid(String supervisorJid);
}
