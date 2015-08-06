package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;

import java.util.List;

public interface ContestModuleDao extends Dao<Long, ContestModuleModel> {

    boolean existByContestJidAndContestModuleName(String contestJid, String contestModuleName);

    ContestModuleModel findByContestJidAndContestModuleName(String contestJid, String contestModuleName);

    List<ContestModuleModel> findEnabledModulesInContestByContestJid(String contestJid);

    List<ContestModuleModel> findEnabledModulesInContestByModuleName(String contestModuleName);
}
