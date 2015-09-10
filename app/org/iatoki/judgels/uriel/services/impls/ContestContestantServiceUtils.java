package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;

final class ContestContestantServiceUtils {

    private ContestContestantServiceUtils() {
        // prevent instantiation
    }

    static ContestContestant createContestContestantFromModel(ContestContestantModel contestContestantModel) {
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status), contestContestantModel.contestStartTime);
    }
}
