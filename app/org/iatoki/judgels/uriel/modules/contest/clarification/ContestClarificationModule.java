package org.iatoki.judgels.uriel.modules.contest.clarification;

import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.TabbedContestModule;
import org.iatoki.judgels.uriel.controllers.routes;
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
        return routes.ContestController.jumpToClarifications(contestId);
    }
}
