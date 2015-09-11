package org.iatoki.judgels.uriel.modules.contest.password;

import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.routes;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.TabbedContestModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ContestPasswordModule extends TabbedContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.PASSWORD;
    }

    @Override
    public String getTabName() {
        return Messages.get("password.passwords");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return contestControllerUtils.isSupervisorOrAbove(contest, userJid);
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return routes.ContestController.jumpToPasswords(contestId);
    }
}
