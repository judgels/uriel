package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;

import java.util.List;

public interface ContestModuleDao extends Dao<Long, ContestModuleModel> {

    boolean existsEnabledInContestByName(String contestJid, String contestModuleName);

    boolean existsInContestByName(String contestJid, String contestModuleName);

    ContestModuleModel findInContestByName(String contestJid, String contestModuleName);

    List<ContestModuleModel> getEnabledInContest(String contestJid);

    List<ContestModuleModel> getEnabledByName(String contestModuleName);
}
