package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;

import java.util.List;

public interface ContestModuleService {

    List<ContestModule> getModulesInContest(String contestJid);

    boolean contestContainsEnabledModule(String contestJid, ContestModules contestModule);

    ContestModule findModuleInContestByType(String contestJid, ContestModules contestModule);

    void enableModule(String contestJid, ContestModules contestModule);

    void disableModule(String contestJid, ContestModules contestModule);
}
