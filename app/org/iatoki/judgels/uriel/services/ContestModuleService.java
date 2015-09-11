package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.modules.contest.ContestModules;

public interface ContestModuleService {

    void enableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress);

    void disableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress);
}
