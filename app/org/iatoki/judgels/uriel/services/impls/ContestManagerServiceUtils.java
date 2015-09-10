package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel;

final class ContestManagerServiceUtils {

    private ContestManagerServiceUtils() {
        // prevent instantiation
    }

    static ContestManager createContestManagerFromModel(ContestManagerModel contestManagerModel) {
        return new ContestManager(contestManagerModel.id, contestManagerModel.contestJid, contestManagerModel.userJid);
    }
}
