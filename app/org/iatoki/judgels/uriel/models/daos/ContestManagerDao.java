package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel;

import java.util.List;

public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean existsByContestJidAndManagerJid(String contestJid, String managerJid);

    ContestManagerModel findByContestJidAndManagerJid(String contestId, String managerJid);

    List<String> findContestJidsByManagerJid(String managerJid);
}
