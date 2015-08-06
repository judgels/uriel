package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;

import java.util.List;

public interface ContestModuleService {

    List<ContestModule> findContestModulesByContestJid(String contestJid);

    boolean containEnabledModule(String contestJid, ContestModules contestModule);

    ContestModule getModule(String contestJid, ContestModules contestModule);

    void enableModule(String contestJid, ContestModules contestModule);

    void disableModule(String contestJid, ContestModules contestModule);
}
