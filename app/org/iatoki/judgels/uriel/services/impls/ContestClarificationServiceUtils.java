package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.play.JidService;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import play.i18n.Messages;

import java.util.Date;

final class ContestClarificationServiceUtils {

    private ContestClarificationServiceUtils() {
        // prevent instantiation
    }

    static ContestClarification createContestClarificationFromModel(ContestProblemDao contestProblemDao, ContestClarificationModel contestClarificationModel, ContestModel contestModel) {
        String topic;
        if ("CONT".equals(JidService.getInstance().parsePrefix(contestClarificationModel.topicJid))) {
            topic = "(" + Messages.get("clarification.general") + ")";
        } else {
            ContestProblemModel contestProblemModel = contestProblemDao.findInContestByJid(contestModel.jid, contestClarificationModel.topicJid);
            topic = contestProblemModel.alias + " - " + JidCacheServiceImpl.getInstance().getDisplayName(contestProblemModel.problemJid);
        }
        return new ContestClarification(contestClarificationModel.id, contestClarificationModel.jid, contestClarificationModel.contestJid, topic, contestClarificationModel.title, contestClarificationModel.question, contestClarificationModel.answer, contestClarificationModel.userCreate, contestClarificationModel.userUpdate, ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate));
    }
}
