package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;

final class ContestProblemServiceUtils {

    private ContestProblemServiceUtils() {
        // prevent instantiation
    }

    static ContestProblem createContestProblemFromModel(ContestProblemModel contestProblemModel) {
        return new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.submissionsLimit, ContestProblemStatus.valueOf(contestProblemModel.status));
    }
}
