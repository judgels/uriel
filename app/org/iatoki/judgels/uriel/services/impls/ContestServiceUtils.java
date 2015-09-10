package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.models.daos.ContestStyleDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModuleFactory;
import org.iatoki.judgels.uriel.modules.ContestModules;

import java.util.List;

final class ContestServiceUtils {

    private ContestServiceUtils() {
        // prevent instantiation
    }

    static Contest createContestFromModel(ContestStyleDao contestStyleDao, ContestModuleFactory contestModuleFactory, ContestModel contestModel, List<ContestModuleModel> contestModuleModels) {
        ContestStyleConfig contestStyleConfig = null;
        ContestStyleModel contestStyleModel = contestStyleDao.findInContest(contestModel.jid);
        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestStyleConfig = new Gson().fromJson(contestStyleModel.config, ICPCContestStyleConfig.class);
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestStyleConfig = new Gson().fromJson(contestStyleModel.config, IOIContestStyleConfig.class);
        }

        ImmutableMap.Builder<ContestModules, ContestModule> contestModuleBuilder = ImmutableMap.builder();
        for (ContestModuleModel contestModuleModel : contestModuleModels) {
            ContestModules contestModules = ContestModules.valueOf(contestModuleModel.name);
            contestModuleBuilder.put(contestModules, contestModuleFactory.parseFromConfig(contestModules, contestModuleModel.config));
        }

        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestStyle.valueOf(contestModel.style), contestStyleConfig, contestModuleBuilder.build());
    }
}
