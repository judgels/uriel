package org.iatoki.judgels.uriel.contest.module.clarification;

import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.TabbedContestModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ContestClarificationModule extends TabbedContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.CLARIFICATION;
    }

    @Override
    public String getTabName() {
        return Messages.get("clarification.clarifications");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return true;
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToClarifications(contestId);
    }
}
