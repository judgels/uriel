package org.iatoki.judgels.uriel.contest.module;

import com.google.inject.ImplementedBy;

@ImplementedBy(ContestModuleServiceImpl.class)
public interface ContestModuleService {

    void enableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress);

    void disableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress);
}
