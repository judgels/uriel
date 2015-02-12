package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;

public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean existsByManagerJid(String contestJid, String managerJid);

    ContestManagerModel findByManagerJid(String contestId, String managerJid);
}
