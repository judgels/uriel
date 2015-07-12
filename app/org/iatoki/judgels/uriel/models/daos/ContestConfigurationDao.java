package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestConfigurationModel;

public interface ContestConfigurationDao extends Dao<Long, ContestConfigurationModel> {

    boolean isExistByContestJid(String contestJid);

    ContestConfigurationModel findByContestJid(String contestJid);

}
