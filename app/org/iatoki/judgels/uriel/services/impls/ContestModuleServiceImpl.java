package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.uriel.models.daos.ContestModuleDao;
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

    private final ContestModuleDao contestModuleDao;
    private final ContestModuleFactory contestModuleFactory;

    @Inject
    public ContestModuleServiceImpl(ContestModuleDao contestModuleDao, ContestModuleFactory contestModuleFactory) {
        this.contestModuleDao = contestModuleDao;
        this.contestModuleFactory = contestModuleFactory;
    }

    @Override
    public void enableModule(String contestJid, ContestModules contestModule) {
        if (contestModuleDao.existsInContestByName(contestJid, contestModule.name())) {
            // TODO check by contest style
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());
            contestModuleModel.enabled = true;

            contestModuleDao.edit(contestModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            ContestModuleModel contestModuleModel = new ContestModuleModel();
            contestModuleModel.contestJid = contestJid;
            contestModuleModel.name = contestModule.name();
            contestModuleModel.config = contestModuleFactory.createDefaultContestModule(contestModule).toJSONString();
            contestModuleModel.enabled = true;

            contestModuleDao.persist(contestModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void disableModule(String contestJid, ContestModules contestModule) {
        if (contestModuleDao.existsInContestByName(contestJid, contestModule.name())) {
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());
            contestModuleModel.enabled = false;

            contestModuleDao.edit(contestModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }
}
