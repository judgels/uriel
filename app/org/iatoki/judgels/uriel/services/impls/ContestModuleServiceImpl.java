package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.uriel.models.daos.ContestModuleDao;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModuleFactory;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.services.ContestModuleService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<ContestModule> getModulesInContest(String contestJid) {
        List<ContestModuleModel> contestModuleModels = contestModuleDao.getEnabledInContest(contestJid);

        return contestModuleModels.stream().map(m -> contestModuleFactory.parseFromConfig(ContestModules.valueOf(m.name), m.config)).collect(Collectors.toList());
    }

    @Override
    public boolean contestContainsEnabledModule(String contestJid, ContestModules contestModule) {
        return contestModuleDao.existsInContestByName(contestJid, contestModule.name());
    }

    @Override
    public ContestModule findModuleInContestByType(String contestJid, ContestModules contestModule) {
        ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());

        return contestModuleFactory.parseFromConfig(contestModule, contestModuleModel.config);
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
