package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel;

import java.util.List;

public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean existsInContestByJid(String contestJid, String managerJid);

    List<String> getContestJidsByJid(String managerJid);
}
