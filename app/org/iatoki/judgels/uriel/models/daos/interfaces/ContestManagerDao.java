package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;

import java.util.List;

public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean existsByContestJidAndManagerJid(String contestJid, String managerJid);

    ContestManagerModel findByContestJidAndManagerJid(String contestId, String managerJid);

    List<String> findContestJidsByManagerJid(String managerJid);
}
