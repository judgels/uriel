package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestModuleDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;
import org.iatoki.judgels.uriel.modules.ContestModuleFactory;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.services.ContestModuleService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("contestModuleService")
public final class ContestModuleServiceImpl implements ContestModuleService {

    private final ContestDao contestDao;
    private final ContestModuleDao contestModuleDao;
    private final ContestModuleFactory contestModuleFactory;

    @Inject
    public ContestModuleServiceImpl(ContestDao contestDao, ContestModuleDao contestModuleDao, ContestModuleFactory contestModuleFactory) {
        this.contestDao = contestDao;
        this.contestModuleDao = contestModuleDao;
        this.contestModuleFactory = contestModuleFactory;
    }

    @Override
    public void enableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress) {
        if (!contestModuleDao.existsInContestByName(contestJid, contestModule.name())) {
            ContestModuleModel contestModuleModel = new ContestModuleModel();
            contestModuleModel.contestJid = contestJid;
            contestModuleModel.name = contestModule.name();
            contestModuleModel.config = contestModuleFactory.createDefaultContestModule(contestModule).toJSONString();
            contestModuleModel.enabled = true;

            contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);

            ContestModel contestModel = contestDao.findByJid(contestJid);

            contestDao.edit(contestModel, userJid, userIpAddress);
        }
    }

    @Override
    public void disableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress) {
        if (contestModuleDao.existsInContestByName(contestJid, contestModule.name())) {
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());
            contestModuleModel.enabled = false;

            contestModuleDao.edit(contestModuleModel, userJid, userIpAddress);

            ContestModel contestModel = contestDao.findByJid(contestJid);

            contestDao.edit(contestModel, userJid, userIpAddress);
        }
    }
}
