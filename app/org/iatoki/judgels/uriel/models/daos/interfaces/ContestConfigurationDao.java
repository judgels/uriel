package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestConfigurationModel;

public interface ContestConfigurationDao extends Dao<Long, ContestConfigurationModel> {

    boolean isExistByContestJid(String contestJid);

    ContestConfigurationModel findByContestJid(String contestJid);

}
