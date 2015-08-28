package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.modules.ContestModules;

public interface ContestModuleService {

    void enableModule(String contestJid, ContestModules contestModule);

    void disableModule(String contestJid, ContestModules contestModule);
}
