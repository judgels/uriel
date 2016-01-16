package org.iatoki.judgels.uriel.contest.module;

import org.iatoki.judgels.uriel.contest.ContestDao;
import org.iatoki.judgels.uriel.contest.ContestModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ContestModuleServiceImpl implements ContestModuleService {

    private final ContestDao contestDao;
    private final ContestModuleDao contestModuleDao;

    @Inject
    public ContestModuleServiceImpl(ContestDao contestDao, ContestModuleDao contestModuleDao) {
        this.contestDao = contestDao;
        this.contestModuleDao = contestModuleDao;
    }

    @Override
    public void enableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress) {
        if (!contestModuleDao.existsInContestByName(contestJid, contestModule.name())) {
            ContestModuleModel contestModuleModel = new ContestModuleModel();
            contestModuleModel.contestJid = contestJid;
            contestModuleModel.name = contestModule.name();
            contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(contestModule).toJSONString();
            contestModuleModel.enabled = true;

            contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        } else {
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());
            contestModuleModel.enabled = true;

            contestModuleDao.edit(contestModuleModel, userJid, userIpAddress);
        }

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void disableModule(String contestJid, ContestModules contestModule, String userJid, String userIpAddress) {
        if (contestModuleDao.existsEnabledInContestByName(contestJid, contestModule.name())) {
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.name());
            contestModuleModel.enabled = false;

            contestModuleDao.edit(contestModuleModel, userJid, userIpAddress);

            ContestModel contestModel = contestDao.findByJid(contestJid);

            contestDao.edit(contestModel, userJid, userIpAddress);
        }
    }
}
