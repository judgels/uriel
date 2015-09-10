package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.ContestPermission;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;

final class ContestSupervisorServiceUtils {

    private ContestSupervisorServiceUtils() {
        // prevent instantiation
    }

    static ContestSupervisor createContestSupervisorFromModel(ContestSupervisorModel contestSupervisorModel) {
        return new ContestSupervisor(contestSupervisorModel.id, contestSupervisorModel.contestJid, contestSupervisorModel.userJid, ContestPermission.fromJSONString(contestSupervisorModel.permission));
    }
}
